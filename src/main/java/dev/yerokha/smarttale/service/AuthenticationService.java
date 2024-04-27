package dev.yerokha.smarttale.service;

import dev.yerokha.smarttale.dto.LoginResponse;
import dev.yerokha.smarttale.dto.RegistrationRequest;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.entity.user.UserEntity;
import dev.yerokha.smarttale.exception.AlreadyTakenException;
import dev.yerokha.smarttale.exception.NotFoundException;
import dev.yerokha.smarttale.repository.InvitationRepository;
import dev.yerokha.smarttale.repository.RoleRepository;
import dev.yerokha.smarttale.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static dev.yerokha.smarttale.util.RedisUtil.deleteKey;
import static dev.yerokha.smarttale.util.RedisUtil.getValue;
import static dev.yerokha.smarttale.util.RedisUtil.setValue;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MailService mailService;
    private final TokenService tokenService;
    private final InvitationRepository invitationRepository;
    public static final int CODE_LENGTH = 4;
    private static final String CHARACTERS = "0123456789";
    private static final SecureRandom random = new SecureRandom();

    public AuthenticationService(UserRepository userRepository,
                                 RoleRepository roleRepository, MailService mailService, TokenService tokenService, InvitationRepository invitationRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.mailService = mailService;
        this.tokenService = tokenService;
        this.invitationRepository = invitationRepository;
    }

    @Transactional
    public String register(RegistrationRequest request) {
        String email = request.email().toLowerCase();
        if (!isEmailAvailable(email)) {
            throw new AlreadyTakenException(String.format("Email %s already taken", email));
        }

        UserEntity entity = new UserEntity(
                email,
                Set.of(roleRepository.findByAuthority("USER")
                        .orElseThrow(() -> new NotFoundException("Role USER not found")))
        );

        UserDetailsEntity details = new UserDetailsEntity(
                request.firstName(),
                request.lastName(),
                request.middleName(),
                email
        );
        setValue(email, entity, 5, TimeUnit.MINUTES);
        setValue("details:" + email, details, 15, TimeUnit.MINUTES);

        sendVerificationEmail(email);

        return email;
    }

    public void sendVerificationEmail(String email) {
        UserEntity user = (UserEntity) getValue(email);

        if (user == null) {
            throw new NotFoundException(String.format("Profile with email %s not found", email));
        }

        user.setVerificationCode(generateVerificationCode());
        setValue(email, user, 15, TimeUnit.MINUTES);

        mailService.sendEmailVerification(email, user.getVerificationCode());
    }

    public LoginResponse verifyEmail(String email, String code) {
        UserEntity user = (UserEntity) getValue(email);
        UserDetailsEntity details = (UserDetailsEntity) getValue("details:" + email);

        if (user == null) {
            throw new NotFoundException(String.format("Profile with email %s not found", email));
        }

        if (!user.getVerificationCode().equals(code)) {
            throw new IllegalArgumentException("Invalid code, please try again");
        }

        if (!user.isEnabled()) {
            user.setEnabled(true);
            if (user.getDetails() == null) {
                details.setUser(user);
                details.setRegisteredAt(LocalDateTime.now());
                user.setDetails(details);
            }
            userRepository.save(user);
        }
        deleteKey(email);
        return new LoginResponse(
                tokenService.generateAccessToken(user),
                tokenService.generateRefreshToken(user),
                user.getUserId()
        );
    }

    public boolean isEmailAvailable(String email) {
        return userRepository.findByEmail(email).isEmpty();
    }

    public LoginResponse refreshToken(String refreshToken) {
        return tokenService.refreshAccessToken(refreshToken);
    }

    public void revoke(String refreshToken, HttpServletRequest request) {
        final String accessToken = request.getHeader("Authorization");
        tokenService.revokeToken(accessToken);
        tokenService.revokeToken(refreshToken);
    }

    private static String generateVerificationCode() {
        return random.ints(AuthenticationService.CODE_LENGTH, 0, CHARACTERS.length())
                .mapToObj(CHARACTERS::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());
    }

    public String login(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(String.format("Profile with email %s not found", email)));

        if (!user.isEnabled()) {
            if (!user.isInvited()) {
                throw new DisabledException("Profile is not enabled");
            }

            user.setEnabled(true);
            invitationRepository.deleteAll(user.getDetails().getInvitations());
            userRepository.save(user);
        }

        user.setVerificationCode(generateVerificationCode());

        setValue(email, user, 15, TimeUnit.MINUTES);

        sendVerificationEmail(email);

        return String.format("Code generated, email sent to %s", email);
    }
}
