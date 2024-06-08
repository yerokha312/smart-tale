package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.dto.AdvertisementInterface;
import dev.yerokha.smarttale.dto.Card;
import dev.yerokha.smarttale.dto.OrderAccepted;
import dev.yerokha.smarttale.dto.OrderDashboard;
import dev.yerokha.smarttale.dto.SearchItem;
import dev.yerokha.smarttale.entity.advertisement.OrderEntity;
import dev.yerokha.smarttale.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @Query("SELECT new dev.yerokha.smarttale.dto.OrderSummaryPersonal(" +
           "    o.advertisementId, " +
           "    SUBSTRING(o.title, 1, 60), " +
           "    SUBSTRING(o.description, 1, 120), " +
           "    COALESCE(o.price, 0), " +
           "    COALESCE((" +
           "        SELECT i.imageUrl " +
           "        FROM AdvertisementImage ai " +
           "        LEFT JOIN ai.image i " +
           "        WHERE ai.advertisement = o AND ai.index = 0), ''), " +
           "    o.publishedAt, " +
           "    CAST((" +
           "        SELECT COUNT (ae.acceptanceId) " +
           "        FROM o.acceptanceEntities ae " +
           "        WHERE ae.order.advertisementId = o.advertisementId) AS INTEGER), " +
           "    o.isClosed) " +
           "FROM OrderEntity o " +
           "WHERE o.publishedBy.userId = :userId AND o.isDeleted = false")
    Page<AdvertisementInterface> findPersonalOrders(@Param("userId") Long userId, Pageable pageable);

    Page<OrderEntity> findAllByPublishedByUserIdAndCompletedAtNotNull(Long userId, Pageable pageable);

    Page<OrderEntity> findAllByPublishedByUserIdAndAcceptedByIsNotNullAndCompletedAtIsNull(Long userId, Pageable pageable);

    @Query("SELECT new dev.yerokha.smarttale.dto.Card(" +
           "    o.advertisementId, " +
           "    o.publishedAt, " +
           "    SUBSTRING(o.title, 1, 60), " +
           "    SUBSTRING(o.description, 1, 120), " +
           "    COALESCE(o.price, 0), " +
           "    COALESCE((" +
           "        SELECT i.imageUrl " +
           "        FROM AdvertisementImage ai " +
           "        LEFT JOIN ai.image i " +
           "        WHERE ai.advertisement = o AND ai.index = 0), ''), " +
           "    o.publishedBy.userId, " +
           "    CONCAT(o.publishedBy.lastName, ' ', o.publishedBy.firstName), " +
           "    COALESCE(pubImg.imageUrl, ''), " +
           "    CASE " +
           "        WHEN NOT EXISTS (" +
           "            SELECT ae " +
           "            FROM AcceptanceEntity ae " +
           "            WHERE ae.order = o AND ae.organization.organizationId = :orgId) THEN true " +
           "        ELSE false " +
           "    END)" +
           "FROM OrderEntity o " +
           "LEFT JOIN o.publishedBy.image pubImg " +
           "WHERE o.isDeleted = false AND o.isClosed = false AND o.acceptedAt IS NULL")
    Page<Card> findMarketOrders(Long orgId, Pageable pageable);

    Optional<OrderEntity> findByPublishedByUserIdAndAdvertisementId(Long userId, Long orderId);

    @Query("SELECT new dev.yerokha.smarttale.dto.OrderAccepted(" +
           "    o.advertisementId, " +
           "    o.taskKey, " +
           "    SUBSTRING(o.title, 1, 60), " +
           "    SUBSTRING(o.description, 1, 120), " +
           "    COALESCE(o.price, 0), " +
           "    COALESCE((" +
           "        SELECT i.imageUrl " +
           "        FROM AdvertisementImage ai " +
           "        LEFT JOIN ai.image i " +
           "        WHERE ai.advertisement = o AND ai.index = 0), ''), " +
           "    o.status, " +
           "    o.acceptedAt, " +
           "    o.deadlineAt, " +
           "    o.completedAt) " +
           "FROM OrderEntity o " +
           "WHERE o.acceptedBy.organizationId = :organizationId " +
           "AND ((:isActive = true AND o.completedAt IS NULL) " +
           "OR (:isActive = false AND o.completedAt IS NOT NULL))")
    Page<OrderAccepted> findByActiveStatus(Long organizationId, boolean isActive, Pageable pageable);

    @Query("SELECT new dev.yerokha.smarttale.dto.OrderAccepted(" +
           "    o.advertisementId, " +
           "    o.taskKey, " +
           "    SUBSTRING(o.title, 1, 60), " +
           "    SUBSTRING(o.description, 1, 120), " +
           "    COALESCE(o.price, 0), " +
           "    COALESCE((" +
           "        SELECT i.imageUrl " +
           "        FROM AdvertisementImage ai " +
           "        LEFT JOIN ai.image i " +
           "        WHERE ai.advertisement = o AND ai.index = 0), ''), " +
           "    o.status, " +
           "    o.acceptedAt, " +
           "    o.deadlineAt, " +
           "    o.completedAt) " +
           "FROM OrderEntity o " +
           "WHERE o.acceptedBy.organizationId = :organizationId " +
           "AND ((:isActive = true AND o.completedAt IS NULL) " +
           "OR (:isActive = false AND o.completedAt IS NOT NULL)) " +
           "AND ((:property = 'accepted' AND o.acceptedAt BETWEEN :dateFrom AND :dateTo) " +
           "OR (:property = 'deadline' AND o.deadlineAt BETWEEN :dateFrom AND :dateTo) " +
           "OR (:property = 'completed' AND o.completedAt BETWEEN :dateFrom AND :dateTo))")
    Page<OrderAccepted> findByDateRange(Long organizationId, boolean isActive, String property, LocalDate dateFrom, LocalDate dateTo, Pageable pageable);

    @Query("SELECT new dev.yerokha.smarttale.dto.OrderDashboard(" +
           "    o.advertisementId, " +
           "    o.status, " +
           "    SUBSTRING(o.title, 1, 60), " +
           "    COALESCE(o.taskKey, ''), " +
           "    COALESCE(o.comment, SUBSTRING(o.description, 1, 120)), " +
           "    o.deadlineAt) " +
           "FROM OrderEntity o " +
           "LEFT JOIN o.acceptanceEntities ae " +
           "LEFT JOIN o.acceptedBy org " +
           "WHERE (org.organizationId = :organizationId AND o.status != :orderStatus) " +
           "OR (ae.organization.organizationId = :organizationId AND ae.requestedAt + 7 DAY >= CURRENT_DATE)")
    List<OrderDashboard> findAllDashboardOrders(Long organizationId, OrderStatus orderStatus);

    @Query("SELECT o " +
           "FROM OrderEntity o " +
           "LEFT JOIN o.contractors co " +
           "WHERE (o.acceptedBy.organizationId = :organizationId AND co.userId = :employeeId) " +
           "AND ((:isActive = true AND o.completedAt IS NULL) " +
           "OR (:isActive = false AND o.completedAt IS NOT NULL))")
    Page<OrderEntity> findTasksByEmployeeId(Long employeeId, Long organizationId, boolean isActive, Pageable pageable);

    @Query("SELECT new dev.yerokha.smarttale.dto.OrderAccepted(" +
           "    o.advertisementId, " +
           "    o.taskKey, " +
           "    SUBSTRING(o.title, 1, 60), " +
           "    SUBSTRING(o.description, 1, 120), " +
           "    COALESCE(o.price, 0), " +
           "    COALESCE((" +
           "        SELECT i.imageUrl " +
           "        FROM AdvertisementImage ai " +
           "        LEFT JOIN ai.image i " +
           "        WHERE ai.advertisement = o AND ai.index = 0), ''), " +
           "    o.status, " +
           "    o.acceptedAt, " +
           "    o.deadlineAt, " +
           "    o.completedAt) " +
           "FROM OrderEntity o " +
           "JOIN o.contractors c " +
           "WHERE c.userId = :userId AND o.completedAt IS NULL")
    List<OrderAccepted> findCurrentOrdersByEmployeeId(Long userId);

    Optional<OrderEntity> findByAcceptedByOrganizationIdAndCompletedAtIsNullAndAdvertisementId(Long organizationId, Long orderId);

    @Query("SELECT new dev.yerokha.smarttale.dto.SearchItem(" +
           "    o.advertisementId, " +
           "    dev.yerokha.smarttale.enums.ContextType.ORDER, " +
           "    o.title, " +
           "    COALESCE((" +
           "        SELECT i.imageUrl " +
           "        FROM AdvertisementImage ai " +
           "        LEFT JOIN ai.image i " +
           "        WHERE ai.advertisement = o AND ai.index = 0), '')) " +
           "FROM OrderEntity o " +
           "WHERE (lower(o.title) LIKE %:query% " +
           "OR lower(o.description) LIKE %:query%) " +
           "AND ((:userId IS NULL AND o.isClosed = false) " +
           "OR (o.publishedBy.userId = :userId)) " +
           "AND o.isDeleted = false")
    Page<SearchItem> findSearchedItemsJPQL(String query, Long userId, Pageable pageable);

    @Query("SELECT new dev.yerokha.smarttale.dto.SearchItem(" +
           "    o.advertisementId, " +
           "    dev.yerokha.smarttale.enums.ContextType.ORDER, " +
           "    o.title, " +
           "    COALESCE((" +
           "        SELECT i.imageUrl " +
           "        FROM AdvertisementImage ai " +
           "        LEFT JOIN ai.image i " +
           "        WHERE ai.advertisement = o AND ai.index = 0), '')) " +
           "FROM OrderEntity o " +
           "WHERE (lower(o.title) LIKE %:query% " +
           "OR lower(o.description) LIKE %:query%) " +
           "AND (o.acceptedBy.organizationId = :organizationId)")
    Page<SearchItem> findSearchedItemsJPQL(String query, Long organizationId, Pageable pageable, String org);

    boolean existsByAdvertisementIdAndPublishedBy_UserId(Long advertisementId, Long userId);

    boolean existsByAcceptedBy_OrganizationIdAndCompletedAtIsNull(Long organizationId);
}







