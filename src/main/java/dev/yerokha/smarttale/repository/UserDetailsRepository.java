package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.dto.SearchItem;
import dev.yerokha.smarttale.dto.UserDto;
import dev.yerokha.smarttale.dto.UserSummary;
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

    @Query("SELECT ud " +
           "FROM UserDetailsEntity ud " +
           "LEFT JOIN ud.invitations inv " +
           "LEFT JOIN ud.organization org " +
           "WHERE org.organizationId = :orgId " +
           "OR (inv.organization.organizationId = :orgId " +
           "AND inv.invitedAt + 7 DAY >= CURRENT_DATE) " +
           "ORDER BY CASE " +
           "    WHEN org.organizationId = :orgId THEN 0 " +
           "    ELSE 1 " +
           "END ASC")
    Page<UserDetailsEntity> findAllEmployeesAndInvitees(@Param("orgId") Long orgId, Pageable pageable);

    @Query("SELECT u " +
           "FROM UserDetailsEntity u " +
           "WHERE u.organization.organizationId = :organizationId " +
           "AND u.userId = :employeeId")
    Optional<UserDetailsEntity> findEmployeeById(Long organizationId, Long employeeId);

    @Modifying
    @Query(value = "UPDATE user_details " +
                   "SET active_orders_count = active_orders_count + :amount " +
                   "WHERE details_id = :userId", nativeQuery = true)
    void updateActiveOrdersCount(int amount, Long userId);

    List<UserDetailsEntity> findAllByOrganizationOrganizationIdAndUserIdIn(Long organizationId, List<Long> employeeIds);

    @Query("SELECT ud.userId " +
           "FROM UserDetailsEntity ud " +
           "WHERE ud.organization.organizationId = :organizationId")
    List<Long> findAllUserIdsByOrganizationId(Long organizationId);

    @Query("SELECT new dev.yerokha.smarttale.dto.SearchItem(" +
           "    ud.userId, " +
           "    CASE " +
           "        WHEN :orgId IS NULL THEN dev.yerokha.smarttale.enums.ContextType.USER " +
           "        ELSE dev.yerokha.smarttale.enums.ContextType.EMPLOYEE " +
           "    END, " +
           "    concat(ud.firstName, ' ', ud.lastName), " +
           "    coalesce(i.imageUrl, '')) " +
           "FROM UserDetailsEntity ud " +
           "LEFT JOIN Image i ON i.imageId = ud.image.imageId " +
           "WHERE (lower(ud.lastName) LIKE %:query% " +
           "OR lower(ud.firstName) LIKE %:query%) " +
           "AND (:orgId IS NULL OR ud.organization.organizationId = :orgId)")
    Page<SearchItem> findSearchedItemsJPQL(@Param("query") String query, @Param("orgId") Long orgId, Pageable pageable);

    @Query("SELECT COUNT(at.advertisementId) > 0 " +
           "FROM UserDetailsEntity u " +
           "JOIN u.assignedTasks at " +
           "WHERE u.userId = :userId")
    boolean existsAssignedTasks(Long userId);

    @Query("SELECT COUNT (e.userId) > 0 " +
           "FROM OrganizationEntity o " +
           "JOIN o.employees e " +
           "WHERE o.organizationId = :orgId AND e.userId = :userId")
    boolean existsInOrganization(Long userId, Long orgId);

    @Query("SELECT u.userId " +
           "FROM UserDetailsEntity u " +
           "WHERE u.position.positionId = :positionId")
    List<Long> findEmployeeIdsByPositionId(Long positionId);

    @Query("SELECT u.email " +
           "FROM UserDetailsEntity u " +
           "WHERE u.position.positionId = :positionId")
    List<String> findEmployeeEmailsByPositionId(Long positionId);

    @Query("SELECT COUNT (o) > 0 " +
           "FROM OrganizationEntity o " +
           "WHERE o.organizationId = :orgId AND o.owner.userId = :userId")
    boolean checkIsOwner(Long userId, Long orgId);

    @Query("SELECT new dev.yerokha.smarttale.dto.UserSummary(" +
           "u.userId, " +
           "CONCAT(u.lastName, u.firstName, COALESCE(u.middleName, '')), " +
           "COALESCE(i.imageUrl, ''), " +
           "CAST(COALESCE(o.organizationId, 0) AS LONG), " +
           "COALESCE(o.name, ''), " +
           "COALESCE(i2.imageUrl, ''), " +
           "u.isSubscribed" +
           ") " +
           "FROM UserDetailsEntity u " +
           "LEFT JOIN u.image i " +
           "LEFT JOIN u.organization o " +
           "LEFT JOIN o.image i2 " +
           "WHERE u.user.isEnabled = true AND u.user.isDeleted = false")
    Page<UserSummary> findAllActiveUsers(Pageable pageable);

    @Query("SELECT new dev.yerokha.smarttale.dto.UserDto(" +
           "    u.userId, " +
           "    CONCAT(u.lastName, u.firstName, COALESCE(u.middleName, '')), " +
           "    COALESCE(i.imageUrl, ''), " +
           "    COALESCE(o.organizationId, 0), " +
           "    COALESCE(o.name, ''), " +
           "    COALESCE(oi.imageUrl, ''), " +
           "    COALESCE(u.position.title, ''), " +
           "    CASE WHEN u.visibleContacts LIKE 'EMAIL%' THEN u.email ELSE '' END, " +
           "    CASE WHEN u.visibleContacts LIKE '%PHONE' THEN u.phoneNumber ELSE '' END, " +
           "    CAST(u.registeredAt AS LOCALDATE), " +
           "    u.isSubscribed" +
           ") " +
           "FROM UserDetailsEntity u " +
           "LEFT JOIN u.image i " +
           "LEFT JOIN u.organization o " +
           "LEFT JOIN o.image oi " +
           "WHERE u.userId = :userId AND u.user.isEnabled = true AND u.user.isDeleted = false")
    Optional<UserDto> findOneUser(Long userId);

}
