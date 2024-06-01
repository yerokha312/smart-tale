package dev.yerokha.smarttale.service;

import dev.yerokha.smarttale.dto.CustomPage;
import dev.yerokha.smarttale.dto.Invitation;
import dev.yerokha.smarttale.dto.Profile;
import dev.yerokha.smarttale.dto.PushNotification;
import dev.yerokha.smarttale.dto.UpdateProfileRequest;
import dev.yerokha.smarttale.entity.Image;
import dev.yerokha.smarttale.entity.user.InvitationEntity;
import dev.yerokha.smarttale.entity.user.OrganizationEntity;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.entity.user.UserEntity;
import dev.yerokha.smarttale.exception.AlreadyTakenException;
import dev.yerokha.smarttale.exception.ForbiddenException;
import dev.yerokha.smarttale.exception.NotFoundException;
import dev.yerokha.smarttale.repository.InvitationRepository;
import dev.yerokha.smarttale.repository.UserDetailsRepository;
import dev.yerokha.smarttale.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;

import static dev.yerokha.smarttale.mapper.CustomPageMapper.getCustomPage;
import static dev.yerokha.smarttale.service.TokenService.getEmailFromAuthToken;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final ImageService imageService;
    private final MailService mailService;
    private final InvitationRepository invitationRepository;

    public UserService(UserRepository userRepository, UserDetailsRepository userDetailsRepository, ImageService imageService, MailService mailService, InvitationRepository invitationRepository) {
        this.userRepository = userRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.imageService = imageService;
        this.mailService = mailService;
        this.invitationRepository = invitationRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));
    }


    public Profile getProfileByUserId(Long userId) {
        UserDetailsEntity userDetails = getUserDetailsEntity(userId);
        return mapProfile(userDetails);
    }

    public Profile updateProfile(Long userId, UpdateProfileRequest request) {
        UserDetailsEntity userDetails = getUserDetailsEntity(userId);

        String newEmail = request.email();
        String newPhoneNumber = request.phoneNumber();

        if (!userDetails.getEmail().equals(newEmail) && !isEmailAvailable(newEmail)) {
            throw new AlreadyTakenException(String.format("Email %s already taken", newEmail));
        }

        if (userDetails.getPhoneNumber() == null || !userDetails.getPhoneNumber().equals(newPhoneNumber)) {
            if (!isPhoneAvailable(newPhoneNumber)) {
                throw new AlreadyTakenException(String.format("Phone number %s already taken", newPhoneNumber));
            }
        }

        UserEntity userEntity = getUserEntity(userId);

        userEntity.setEmail(newEmail);
        userDetails.setEmail(newEmail);
        userDetails.setFirstName(request.firstName());
        userDetails.setLastName(request.lastName());
        userDetails.setMiddleName(request.middleName());
        userDetails.setPhoneNumber(newPhoneNumber);
        userDetailsRepository.save(userDetails);
        return mapProfile(userDetails);
    }

    private boolean isEmailAvailable(String email) {
        return userRepository.findByEmail(email).isEmpty();
    }

    private boolean isPhoneAvailable(String phoneNumber) {
        return userDetailsRepository.findByPhoneNumber(phoneNumber).isEmpty();
    }


    private Profile mapProfile(UserDetailsEntity userDetails) {
        Image avatar = userDetails.getImage();
        OrganizationEntity organization = userDetails.getOrganization();
        return new Profile(
                userDetails.getFirstName(),
                userDetails.getLastName(),
                userDetails.getMiddleName() == null ? "" : userDetails.getMiddleName(),
                userDetails.getEmail(),
                userDetails.getPhoneNumber(),
                avatar == null ? "" : avatar.getImageUrl(),
                organization == null ? 0 : organization.getOrganizationId(),
                organization == null ? "" : organization.getName(),
                organization != null && organization.getImage() != null ? organization.getImage().getImageUrl() : "",
                userDetails.isSubscribed() ? userDetails.getSubscriptionEndDate() : null
        );
    }

    public void uploadAvatar(MultipartFile avatar, Long userIdFromAuthToken) {
        UserDetailsEntity details = getUserDetailsEntity(userIdFromAuthToken);

        details.setImage(imageService.processImage(avatar));

        userDetailsRepository.save(details);

    }

    UserEntity getUserEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    UserDetailsEntity getUserDetailsEntity(Long userId) {
        return userDetailsRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User details not found"));
    }

    public void subscribe(Long userIdFromAuthToken) {
        UserDetailsEntity user = getUserDetailsEntity(userIdFromAuthToken);
        mailService.sendSubscriptionRequest(user);
    }

    public CustomPage<Invitation> getInvitations(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Invitation> invitations = invitationRepository
                .findAllByInviteeId(userId, pageable);
        return getCustomPage(invitations);
    }

    @Transactional
    public PushNotification acceptInvitation(Authentication authentication, Long invitationId) {
        Long userId = TokenService.getUserIdFromAuthToken(authentication);
        boolean userHasAssignedTasks = userDetailsRepository.existsAssignedTasks(userId);
        if (userHasAssignedTasks) {
            throw new ForbiddenException("You have assigned tasks");
        }

        InvitationEntity invitation = invitationRepository.findByInvitationIdAndInvitee_UserIdAndInvitedAtBefore(
                        invitationId, userId, LocalDateTime.now().plusWeeks(1))
                .orElseThrow(() -> new NotFoundException("Invitation not found or expired"));

        UserDetailsEntity user = userDetailsRepository.getReferenceById(userId);
        OrganizationEntity organization = invitation.getOrganization();
        user.setOrganization(organization);
        user.setPosition(invitation.getPosition());
        user.setActiveOrdersCount(0);

        userDetailsRepository.save(user);
        invitationRepository.deleteAllByInvitee_UserIdJPQL(userId);

        String email = getEmailFromAuthToken(authentication);
        Image image = invitation.getInvitee().getImage();
        String imageUrl = image == null ? "" : image.getImageUrl();
        Map<String, String> data = Map.of(
                "email", email,
                "sub", "Пользователь принял приглашение",
                "employeeId", userId.toString(),
                "employeeName", invitation.getInvitee().getName(),
                "employeeAvatar", imageUrl,
                "employeePosition", invitation.getPosition().getTitle()
        );

        return new PushNotification(
                organization.getOrganizationId(),
                data
        );
    }

    public void declineInvitation(Long userId, Long invitationId) {
        int deletedCount = invitationRepository.deleteByInvitationIdAndInvitee_UserIdJPQL(invitationId, userId);
        if (deletedCount == 0) {
            throw new NotFoundException("Invitation not found");
        }
    }
}
