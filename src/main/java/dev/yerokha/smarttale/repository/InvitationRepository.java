package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.entity.user.InvitationEntity;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<InvitationEntity, Long> {
    boolean existsByInviteeEmail(String email);

    @Query("SELECT i.invitee FROM " +
           "InvitationEntity i " +
           "WHERE i.organization.organizationId = :organizationId " +
           "AND i.invitee.userId = :userId")
    Optional<UserDetailsEntity> findInviteeById(Long organizationId, Long userId);
}
