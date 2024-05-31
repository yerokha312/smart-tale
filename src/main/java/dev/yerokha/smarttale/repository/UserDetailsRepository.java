package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.dto.SearchItem;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDetailsRepository extends JpaRepository<UserDetailsEntity, Long> {

    Optional<UserDetailsEntity> findByPhoneNumber(String phoneNumber);

    Optional<UserDetailsEntity> findByEmail(String email);

    @Query("SELECT DISTINCT ud FROM UserDetailsEntity ud " +
           "LEFT JOIN ud.invitations inv " +
           "LEFT JOIN ud.organization org " +
           "WHERE org.organizationId = :orgId OR (inv.organization.organizationId = :orgId " +
           "AND inv.invitedAt + 7 DAY >= CURRENT_DATE)")
    Page<UserDetailsEntity> findAllEmployeesAndInvitees(@Param("orgId") Long orgId, Pageable pageable);

    @Query("SELECT u " +
           "FROM UserDetailsEntity u " +
           "WHERE u.organization.organizationId = :organizationId " +
           "AND u.userId = :employeeId")
    Optional<UserDetailsEntity> findEmployeeById(Long organizationId, Long employeeId);

    @Modifying
    @Query(value = "UPDATE user_details SET active_orders_count = active_orders_count + :amount WHERE details_id = :userId", nativeQuery = true)
    void updateActiveOrdersCount(int amount, Long userId);

    List<UserDetailsEntity> findAllByOrganizationOrganizationIdAndUserIdIn(Long organizationId, List<Long> employeeIds);

    @Query("SELECT ud.userId " +
           "FROM UserDetailsEntity ud " +
           "WHERE ud.organization.organizationId = :organizationId")
    List<Long> findAllUserIdsByOrganizationId(Long organizationId);

    @Query("SELECT new dev.yerokha.smarttale.dto.SearchItem(" +
           "ud.userId, " +
           "CASE WHEN :orgId IS NULL THEN dev.yerokha.smarttale.enums.ContextType.USER " +
           "ELSE dev.yerokha.smarttale.enums.ContextType.EMPLOYEE END, " +
           "concat(ud.firstName, ' ', ud.lastName), " +
           "coalesce(i.imageUrl, '')" +
           ") " +
           "FROM UserDetailsEntity ud " +
           "LEFT JOIN Image i ON i.imageId = ud.image.imageId " +
           "WHERE (lower(ud.lastName) LIKE %:query% " +
           "OR lower(ud.firstName) LIKE %:query%) " +
           "AND (:orgId IS NULL OR ud.organization.organizationId = :orgId)")
    Page<SearchItem> findSearchedItemsJPQL(@Param("query") String query, @Param("orgId") Long orgId, Pageable pageable);

}
