package dev.yerokha.smarttale.service;

import dev.yerokha.smarttale.dto.LoginResponse;
import dev.yerokha.smarttale.dto.RegistrationRequest;
import dev.yerokha.smarttale.entity.user.InvitationEntity;
import dev.yerokha.smarttale.entity.user.Role;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.entity.user.UserEntity;
import dev.yerokha.smarttale.enums.OrderStatus;
import dev.yerokha.smarttale.exception.AlreadyTakenException;
import dev.yerokha.smarttale.exception.NotFoundException;
import dev.yerokha.smarttale.repository.InvitationRepository;
import dev.yerokha.smarttale.repository.RoleRepository;
import dev.yerokha.smarttale.repository.UserRepository;
import dev.yerokha.smarttale.util.EncryptionUtil;
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
                                 RoleRepository roleRepository, MailService mailService, TokenService tokenService,
                                 InvitationRepository invitationRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.mailService = mailService;
        this.tokenService = tokenService;
        this.invitationRepository = invitationRepository;
    }

    private String register(RegistrationRequest request) {
        String email = request.email().toLowerCase();
        boolean userIsInvited = isUserInvited(email);
        if (!isEmailAvailable(email) && !userIsInvited) {
            throw new AlreadyTakenException(String.format("Email %s already taken", email));
        }

        UserEntity user = new UserEntity(
                email,
                getUserRole()
        );

        UserDetailsEntity details = new UserDetailsEntity(
                request.firstName(),
                request.lastName(),
                request.middleName(),
                email
        );

        if (userIsInvited) {
            user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new NotFoundException("User not found"));
            user.setInvited(false);
            user.setAuthorities(getUserRole());
            details = user.getDetails();
            details.setLastName(request.lastName());
            details.setFirstName(request.firstName());
            details.setMiddleName(request.middleName());
        }

        setValue(email, user, 5, TimeUnit.MINUTES);
        setValue("details:" + email, details, 15, TimeUnit.MINUTES);

        sendVerificationEmail(email);

        return email;
    }

    private Set<Role> getUserRole() {
        return Set.of(roleRepository.findByAuthority("USER")
                .orElseThrow(() -> new NotFoundException("Role USER not found")));
    }

    public void sendVerificationEmail(String email) {
        UserEntity user = (UserEntity) getValue(email);

        if (user == null) {
            throw new NotFoundException(String.format("User with email %s not found", email));
        }

        if (user.isEnabled()) {
            sendLoginEmail(email);
        } else {
            user.setVerificationCode(generateVerificationCode());
            setValue(email, user, 15, TimeUnit.MINUTES);
            mailService.sendEmailVerificationCode(email, user.getVerificationCode());
        }
    }

    public LoginResponse verifyEmail(String email, String code) {
        UserEntity user = (UserEntity) getValue(email);
        UserDetailsEntity details = (UserDetailsEntity) getValue("details:" + email);

        if (user == null) {
            throw new NotFoundException(String.format("User with email %s not found", email));
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

    private boolean isUserInvited(String email) {
        return invitationRepository.existsByInviteeEmail(email);
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

    private String login(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new NotFoundException(String.format("User with email %s not found", email)));

        if (!user.isEnabled()) {
            throw new DisabledException("User is not enabled");
        }

        user.setVerificationCode(generateVerificationCode());

        setValue(email, user, 15, TimeUnit.MINUTES);

        sendLoginEmail(email);

        return String.format("Code generated, email sent to %s", email);
    }

    private void sendLoginEmail(String email) {
        UserEntity user = (UserEntity) getValue(email);

        if (user == null) {
            throw new NotFoundException(String.format("User with email %s not found", email));
        }

        user.setVerificationCode(generateVerificationCode());
        setValue(email, user, 15, TimeUnit.MINUTES);

        mailService.sendLoginCode(email, user.getVerificationCode());
    }


    @Transactional
    public String register(RegistrationRequest request, String code) {
        if (code == null) {
            return register(request);
        }

        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new NotFoundException("User not found"));
        UserDetailsEntity details = user.getDetails();

        InvitationEntity invitation = invitationRepository.findById(
                        Long.valueOf(EncryptionUtil.decrypt(code)))
                .orElseThrow(() -> new NotFoundException("Invitation not found"));

        user.setAuthorities(getUserRole());
        user.setEnabled(true);
        user.setInvited(false);
        details.setLastName(request.lastName());
        details.setFirstName(request.firstName());
        details.setMiddleName(request.middleName());
        details.setOrganization(invitation.getOrganization());
        details.setPosition(invitation.getPosition());
        details.setRegisteredAt(LocalDateTime.now());

        userRepository.save(user);
        invitationRepository.deleteAll(details.getInvitations());

        return "Registered successfully. Please log in";
    }

    @Transactional
    public String login(String email, String code) {
        if (code == null) {
            return login(email);
        }

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new NotFoundException(String.format("User with email %s not found", email)));

        InvitationEntity invitation = invitationRepository.findById(
                        Long.valueOf(EncryptionUtil.decrypt(code)))
                .orElseThrow(() -> new NotFoundException("Invitation not found"));

        UserDetailsEntity details = user.getDetails();
        details.setOrganization(invitation.getOrganization());
        details.setPosition(invitation.getPosition());
        details.getAcceptedOrders().removeIf(order -> !order.getStatus().equals(OrderStatus.ARRIVED));
        details.setActiveOrdersCount(0);

        userRepository.save(user);
        invitationRepository.deleteAll(details.getInvitations());

        if (!user.isEnabled()) {
            throw new DisabledException("User is not enabled");
        }

        user.setVerificationCode(generateVerificationCode());

        setValue(email, user, 15, TimeUnit.MINUTES);

        sendLoginEmail(email);

        return String.format("Code generated, email sent to %s", email);
    }
}



























