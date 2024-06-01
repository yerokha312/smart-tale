package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.dto.Invitation;
import dev.yerokha.smarttale.entity.user.InvitationEntity;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<InvitationEntity, Long> {
    boolean existsByInviteeEmail(String email);

    @Query("SELECT i.invitee FROM " +
           "InvitationEntity i " +
           "WHERE i.organization.organizationId = :organizationId " +
           "AND i.invitee.userId = :userId")
    Optional<UserDetailsEntity> findInviteeById(Long organizationId, Long userId);

    @Query("SELECT new dev.yerokha.smarttale.dto.Invitation(" +
           "i.invitationId, " +
           "o.organizationId, " +
           "o.name, " +
           "COALESCE(oi.imageUrl, ''), " +
           "p.title, " +
           "i.invitedAt" +
           ")" +
           "FROM InvitationEntity i " +
           "JOIN i.organization o " +
           "LEFT JOIN o.image oi " +
           "JOIN i.position p " +
           "WHERE i.invitee.userId = :userId AND i.invitedAt + 7 DAY >= CURRENT DATE " +
           "AND i.invitedAt = (" +
           "SELECT MAX(i2.invitedAt) " +
           "FROM InvitationEntity i2 " +
           "WHERE i2.organization.organizationId = o.organizationId AND i2.invitee.userId = :userId) " +
           "ORDER BY i.invitedAt DESC")
    Page<Invitation> findAllByInviteeId(Long userId, Pageable pageable);

    @Query("SELECT i " +
           "FROM InvitationEntity i " +
           "WHERE i.invitee.userId = :userId " +
           "AND i.organization.organizationId = :organizationId " +
           "AND i.invitedAt = (" +
           "SELECT MAX(i2.invitedAt) " +
           "FROM InvitationEntity i2 " +
           "WHERE i2.organization.organizationId = :organizationId AND i2.invitee.userId = :userId)")
    Optional<InvitationEntity> findByInviteeIdAndOrganizationId(Long userId, Long organizationId);

    Optional<InvitationEntity> findByInvitationIdAndInvitee_UserIdAndInvitedAtBefore(Long invitationId, Long inviteeId, LocalDateTime week);

    @Modifying
    @Query("DELETE FROM InvitationEntity i WHERE i.invitee.userId = :userId")
    void deleteAllByInvitee_UserIdJPQL(Long userId);
}
