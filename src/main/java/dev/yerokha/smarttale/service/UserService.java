package dev.yerokha.smarttale.service;

import dev.yerokha.smarttale.dto.Profile;
import dev.yerokha.smarttale.dto.UpdateProfileRequest;
import dev.yerokha.smarttale.entity.Image;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.entity.user.UserEntity;
import dev.yerokha.smarttale.exception.AlreadyTakenException;
import dev.yerokha.smarttale.exception.NotFoundException;
import dev.yerokha.smarttale.repository.UserDetailsRepository;
import dev.yerokha.smarttale.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final ImageService imageService;
    private final MailService mailService;

    public UserService(UserRepository userRepository, UserDetailsRepository userDetailsRepository, ImageService imageService, MailService mailService) {
        this.userRepository = userRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.imageService = imageService;
        this.mailService = mailService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return userRepository.findByEmail(email).orElseThrow(() ->
                new NotFoundException("Profile not found"));
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
        return new Profile(
                userDetails.getFirstName(),
                userDetails.getLastName(),
                userDetails.getMiddleName(),
                userDetails.getEmail(),
                userDetails.getPhoneNumber(),
                avatar == null ? null : avatar.getImageUrl(),
                userDetails.isSubscribed() ? userDetails.getSubscriptionEndDate() : null
        );
    }

    public void uploadAvatar(MultipartFile avatar, Long userIdFromAuthToken) {
        UserDetailsEntity details = getUserDetailsEntity(userIdFromAuthToken);

        details.setImage(imageService.processImage(avatar));

        userDetailsRepository.save(details);

    }

    private UserEntity getUserEntity(Long userIdFromAuthToken) {
        return userRepository.findById(userIdFromAuthToken)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private UserDetailsEntity getUserDetailsEntity(Long userIdFromAuthToken) {
        return userDetailsRepository.findById(userIdFromAuthToken)
                .orElseThrow(() -> new NotFoundException("User details not found"));
    }

    public void subscribe(Long userIdFromAuthToken) {
        UserDetailsEntity user = getUserDetailsEntity(userIdFromAuthToken);
        mailService.sendSubscriptionRequest(user);

    }
}
