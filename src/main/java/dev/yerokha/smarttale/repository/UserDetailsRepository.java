package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDetailsRepository extends JpaRepository<UserDetailsEntity, Long> {

    Optional<UserDetailsEntity> findByPhoneNumber(String phoneNumber);

    Optional<UserDetailsEntity> findByEmail(String email);

    @Query("SELECT DISTINCT ud FROM UserDetailsEntity ud " +
            "LEFT JOIN ud.invitations inv " +
            "LEFT JOIN ud.organization org " +
            "WHERE org.organizationId = :orgId OR inv.organization.organizationId = :orgId")
    Page<UserDetailsEntity> findAllEmployeesAndInvitees(@Param("orgId") Long orgId, Pageable pageable);
}
