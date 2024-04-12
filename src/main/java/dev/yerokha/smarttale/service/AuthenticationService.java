package dev.yerokha.smarttale.service;

import dev.yerokha.smarttale.dto.LoginResponse;
import dev.yerokha.smarttale.dto.RegistrationRequest;
import dev.yerokha.smarttale.entity.user.UserEntity;
import dev.yerokha.smarttale.exception.EmailAlreadyTakenException;
import dev.yerokha.smarttale.exception.NotFoundException;
import dev.yerokha.smarttale.repository.RoleRepository;
import dev.yerokha.smarttale.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
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

    public static final int CODE_LENGTH = 6;
    private static final String CHARACTERS = "0123456789";
    private static final SecureRandom random = new SecureRandom();

    public AuthenticationService(UserRepository userRepository,
                                 RoleRepository roleRepository, MailService mailService, TokenService tokenService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.mailService = mailService;
        this.tokenService = tokenService;
    }

    @Transactional
    public String register(RegistrationRequest request) {
        String email = request.email().toLowerCase();
        if (!isEmailAvailable(email)) {
            throw new EmailAlreadyTakenException(String.format("Email %s already taken", email));
        }

        UserEntity entity = new UserEntity(
                request.firstName(),
                request.lastName(),
                request.fatherName(),
                email,
                Set.of(roleRepository.findByAuthority("USER")
                        .orElseThrow(() -> new NotFoundException("Role USER not found")))
        );

        setValue(email, entity, 5, TimeUnit.MINUTES);

        sendVerificationEmail(email);

        return email;
    }

    public void sendVerificationEmail(String email) {
        UserEntity user = (UserEntity) getValue(email);

        if (user == null) {
            throw new NotFoundException(String.format("User with email %s not found", email));
        }

        user.setVerificationCode(generateVerificationCode());
        setValue(email, user, 15, TimeUnit.MINUTES);

        mailService.sendEmailVerification(email, user.getFirstName() +
                (user.getFatherName() == null ? "" : " " + user.getFatherName()), user.getVerificationCode());
    }

    public LoginResponse verifyEmail(String email, String code) {
        UserEntity user = (UserEntity) getValue(email);

        if (user == null) {
            throw new NotFoundException(String.format("User with email %s not found", email));
        }

        if (!user.getVerificationCode().equals(code)) {
            throw new IllegalArgumentException("Invalid code, please try again");
        }

        user.setEnabled(true);
        userRepository.save(user);
        deleteKey(email);
        return new LoginResponse(
                tokenService.generateAccessToken(user),
                tokenService.generateRefreshToken(user),
                user.getUserId(),
                user.getName()
        );
    }

    public boolean isEmailAvailable(String email) {
        return userRepository.findByEmail(email).isEmpty();
    }

    private static String generateVerificationCode() {
        return random.ints(AuthenticationService.CODE_LENGTH, 0, CHARACTERS.length())
                .mapToObj(CHARACTERS::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());
    }
}
