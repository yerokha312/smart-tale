package dev.yerokha.smarttale.service;

import dev.yerokha.smarttale.dto.User;
import dev.yerokha.smarttale.entity.Image;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.entity.user.UserEntity;
import dev.yerokha.smarttale.exception.NotFoundException;
import dev.yerokha.smarttale.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return userRepository.findByEmail(email).orElseThrow(() ->
                new NotFoundException("User not found"));
    }

    public User getProfileByUserId(Long userIdFromAuthToken) {
        UserEntity userEntity = userRepository.findById(userIdFromAuthToken)
                .orElseThrow(() -> new NotFoundException("User not found"));
        UserDetailsEntity userDetails = userEntity.getDetails();
        Image avatar = userDetails.getImage();
        return new User(
                userEntity.getFirstName(),
                userEntity.getLastName(),
                userEntity.getFatherName(),
                userEntity.getEmail(),
                userEntity.getPhoneNumber(),
                avatar == null ? null : avatar.getImageUrl(),
                userDetails.isSubscribed() ? userDetails.getSubscriptionEndDate() : null
        );

    }
}
