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

    public UserService(UserRepository userRepository, UserDetailsRepository userDetailsRepository, ImageService imageService) {
        this.userRepository = userRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.imageService = imageService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return userRepository.findByEmail(email).orElseThrow(() ->
                new NotFoundException("Profile not found"));
    }

    public Profile getProfileByUserId(Long userIdFromAuthToken) {
        UserEntity userEntity = getUserEntity(userIdFromAuthToken);
        return mapProfile(userEntity);
    }

    public Profile updateProfile(Long userIdFromAuthToken, UpdateProfileRequest request) {
        UserEntity userEntity = getUserEntity(userIdFromAuthToken);

        String newEmail = request.email();
        String newPhoneNumber = request.phoneNumber();

        if (!userEntity.getEmail().equals(newEmail) && !isEmailAvailable(newEmail)) {
            throw new AlreadyTakenException(String.format("Email %s already taken", newEmail));
        }

        if (userEntity.getPhoneNumber() == null || !userEntity.getPhoneNumber().equals(newPhoneNumber)) {
            if (!isPhoneAvailable(newPhoneNumber)) {
                throw new AlreadyTakenException(String.format("Phone number %s already taken", newPhoneNumber));
            }
        }

        userEntity.setFirstName(request.firstName());
        userEntity.setLastName(request.lastName());
        userEntity.setFatherName(request.fatherName());
        userEntity.setEmail(request.email());
        userEntity.setPhoneNumber(request.phoneNumber());
        userRepository.save(userEntity);
        return mapProfile(userEntity);
    }

    private boolean isEmailAvailable(String email) {
        return userRepository.findByEmail(email).isEmpty();
    }

    private boolean isPhoneAvailable(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber).isEmpty();
    }


    private Profile mapProfile(UserEntity userEntity) {
        UserDetailsEntity userDetails = userEntity.getDetails();
        Image avatar = userDetails.getImage();
        return new Profile(
                userEntity.getFirstName(),
                userEntity.getLastName(),
                userEntity.getFatherName(),
                userEntity.getEmail(),
                userEntity.getPhoneNumber(),
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
}
