package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDetailsRepository extends JpaRepository<UserDetailsEntity, Long> {

    Optional<UserDetailsEntity> findByPhoneNumber(String phoneNumber);
    Optional<UserDetailsEntity> findByEmail(String email);
}
