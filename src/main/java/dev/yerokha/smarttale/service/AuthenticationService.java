package dev.yerokha.smarttale.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.yerokha.smarttale.dto.LoginResponse;
import dev.yerokha.smarttale.dto.RegistrationRequest;
import dev.yerokha.smarttale.entity.user.InvitationEntity;
import dev.yerokha.smarttale.entity.user.PositionEntity;
import dev.yerokha.smarttale.entity.user.Role;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.entity.user.UserEntity;
import dev.yerokha.smarttale.exception.AlreadyTakenException;
import dev.yerokha.smarttale.exception.MissedException;
import dev.yerokha.smarttale.exception.NotFoundException;
import dev.yerokha.smarttale.repository.InvitationRepository;
import dev.yerokha.smarttale.repository.RoleRepository;
import dev.yerokha.smarttale.repository.UserDetailsRepository;
import dev.yerokha.smarttale.repository.UserRepository;
import dev.yerokha.smarttale.util.Authorities;
import dev.yerokha.smarttale.util.EncryptionUtil;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static dev.yerokha.smarttale.util.RedisUtil.deleteKey;
import static dev.yerokha.smarttale.util.RedisUtil.getValue;
import static dev.yerokha.smarttale.util.RedisUtil.setValue;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final RoleRepository roleRepository;
    private final MailService mailService;
    private final TokenService tokenService;
    private final InvitationRepository invitationRepository;
    private final ObjectMapper objectMapper;
    public static final int CODE_LENGTH = 4;
    private static final String CHARACTERS = "0123456789";
    private static final SecureRandom random = new SecureRandom();

    public AuthenticationService(UserRepository userRepository, UserDetailsRepository userDetailsRepository,
                                 RoleRepository roleRepository, MailService mailService, TokenService tokenService,
                                 InvitationRepository invitationRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.roleRepository = roleRepository;
        this.mailService = mailService;
        this.tokenService = tokenService;
        this.invitationRepository = invitationRepository;
        this.objectMapper = objectMapper;
    }

    public void register(RegistrationRequest request) {
        String email = request.email().toLowerCase();
        boolean userIsInvited = isUserInvited(email);
        if (!isEmailAvailable(email) && !userIsInvited) {
            throw new AlreadyTakenException(String.format("Email %s already taken", email));
        }
        if (!isPhoneAvailable(request.phoneNumber()) && !userIsInvited) {
            throw new AlreadyTakenException(String.format("Phone number %s already taken", request.phoneNumber()));
        }

        UserEntity user = new UserEntity();
        user.setEmail(email);
        UserDetailsEntity details = new UserDetailsEntity(
                request.firstName(),
                request.lastName(),
                request.middleName(),
                email,
                request.phoneNumber()
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

        String userJson;
        String detailsJson;
        try {
            userJson = objectMapper.writeValueAsString(user);
            detailsJson = objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize object");
        }
        setValue(email, userJson, 15, TimeUnit.MINUTES);
        setValue("details:" + email, detailsJson, 15, TimeUnit.MINUTES);

        sendVerificationEmail(email);
    }

    public boolean isPhoneAvailable(String phoneNumber) {
        return userDetailsRepository.findByPhoneNumber(phoneNumber).isEmpty();
    }

    public Set<Role> getUserRole() {
        return Set.of(roleRepository.findByAuthority("USER")
                .orElseThrow(() -> new NotFoundException("Role USER not found")));
    }

    public Set<Role> getUserAndEmployeeRole() {
        List<Role> roleList = roleRepository.findAll();
        roleList.remove(2);
        return new HashSet<>(roleList);
    }

    public void sendVerificationEmail(String email) {
        UserEntity user;

        try {
            user = objectMapper.readValue(getValue(email), UserEntity.class);
        } catch (Exception e) {
            throw new NotFoundException(String.format("User with email %s not found", email));
        }

        if (user.isEnabled()) {
            sendLoginEmail(email);
        } else {
            user.setVerificationCode(generateVerificationCode());
            String userJson;
            try {
                userJson = objectMapper.writeValueAsString(user);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Failed to serialize object");
            }
            setValue(email, userJson, 15, TimeUnit.MINUTES);
            mailService.sendEmailVerificationCode(email, user.getVerificationCode());
        }
    }

    public LoginResponse verifyEmail(String email, String code) {
        UserEntity user;
        try {
            user = objectMapper.readValue(getValue(email), UserEntity.class);
        } catch (Exception e) {
            throw new NotFoundException("User not found");
        }

        if (!user.getVerificationCode().equals(code)) {
            throw new IllegalArgumentException("Invalid code, please try again");
        }

        if (!user.isEnabled()) {
            user.setEnabled(true);
            user.setAuthorities(getUserRole());
            UserDetailsEntity details;
            try {
                details = objectMapper.readValue(getValue("details:" + email), UserDetailsEntity.class);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Failed to serialize object");
            }
            details.setUser(user);
            details.setRegisteredAt(LocalDateTime.now());
            user.setDetails(details);
            userRepository.save(user);
        }

        deleteKey(email);
        deleteKey("details:" + email);

        UserDetailsEntity userDetails = userDetailsRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User details not found"));
        PositionEntity position = userDetails.getPosition();
        return new LoginResponse(
                tokenService.generateAccessToken(user, position),
                tokenService.generateRefreshToken(user, position),
                user.getUserId(),
                position == null ? 0 : position.getOrganization().getOrganizationId(),
                position == null ? 0 : position.getHierarchy(),
                position == null ? Collections.emptyList() : Authorities.getNamesByValues(position.getAuthorities())
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

    public void revoke(String refreshToken) {
        tokenService.revokeToken_Logout(refreshToken);
    }

    private static String generateVerificationCode() {
        return random.ints(AuthenticationService.CODE_LENGTH, 0, CHARACTERS.length())
                .mapToObj(CHARACTERS::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());
    }

    public String login(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new NotFoundException(String.format("User with email %s not found", email)));

        if (!user.isEnabled()) {
            throw new DisabledException("User is not enabled");
        }

        if (user.isDeleted()) {
            return "Your account is deleted, please restore before login";
        }

        String userJson;
        try {
            userJson = objectMapper.writeValueAsString(user);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize object");
        }

        setValue(email, userJson, 15, TimeUnit.MINUTES);

        sendLoginEmail(email);

        return String.format("Code generated, email sent to %s", email);
    }

    private void sendLoginEmail(String email) {
        UserEntity user;

        try {
            user = objectMapper.readValue(getValue(email), UserEntity.class);
        } catch (Exception e) {
            throw new NotFoundException(String.format("User with email %s not found", email));
        }

        user.setVerificationCode(generateVerificationCode());
        String userJson;
        try {
            userJson = objectMapper.writeValueAsString(user);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize object");
        }
        setValue(email, userJson, 15, TimeUnit.MINUTES);

        mailService.sendLoginCode(email, user.getVerificationCode());
    }


    @Transactional
    public void register(RegistrationRequest request, String code) {
        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new NotFoundException("User not found"));
        UserDetailsEntity details = user.getDetails();

        InvitationEntity invitation = invitationRepository.findById(
                        Long.valueOf(EncryptionUtil.decrypt(code)))
                .orElseThrow(() -> new NotFoundException("Invitation not found"));

        if (invitation.getInvitedAt().plusDays(7).isBefore(LocalDateTime.now())) {
            throw new MissedException("Invitation is expired");
        }

        user.setAuthorities(getUserAndEmployeeRole());
        user.setEnabled(true);
        user.setInvited(false);
        details.setLastName(request.lastName());
        details.setFirstName(request.firstName());
        details.setMiddleName(request.middleName());
        details.setOrganization(invitation.getOrganization());
        details.setPosition(invitation.getPosition());
        details.setRegisteredAt(LocalDateTime.now());

        userRepository.save(user);
        invitationRepository.deleteAllByInvitee_UserIdJPQL(user.getUserId());
    }

    @Transactional
    public String login(String email, String code) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new NotFoundException(String.format("User with email %s not found", email)));

        InvitationEntity invitation = invitationRepository.findById(
                        Long.valueOf(EncryptionUtil.decrypt(code)))
                .orElseThrow(() -> new NotFoundException("Invitation not found"));

        if (invitation.getInvitedAt().plusDays(7).isBefore(LocalDateTime.now())) {
            throw new MissedException("Invitation is expired");
        }

        UserDetailsEntity details = user.getDetails();
        details.setOrganization(invitation.getOrganization());
        details.setPosition(invitation.getPosition());
        details.getAssignedTasks().clear();
        details.setActiveOrdersCount(0);

        user.setAuthorities(getUserAndEmployeeRole());
        userRepository.save(user);
        invitationRepository.deleteAllByInvitee_UserIdJPQL(user.getUserId());

        if (!user.isEnabled()) {
            throw new DisabledException("User is not enabled");
        }

        String userJson;
        try {
            userJson = objectMapper.writeValueAsString(user);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize object");
        }

        setValue(email, userJson, 15, TimeUnit.MINUTES);

        sendLoginEmail(email);

        return String.format("Code generated, email sent to %s", email);
    }
}
