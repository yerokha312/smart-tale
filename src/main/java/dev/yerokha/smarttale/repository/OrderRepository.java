package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.dto.SearchItem;
import dev.yerokha.smarttale.entity.advertisement.OrderEntity;
import dev.yerokha.smarttale.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    Page<OrderEntity> findAllByPublishedByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);

    Page<OrderEntity> findAllByPublishedByUserIdAndCompletedAtNotNull(Long userId, Pageable pageable);

    Page<OrderEntity> findAllByPublishedByUserIdAndAcceptedByIsNotNullAndCompletedAtIsNull(Long userId, Pageable pageable);

    Page<OrderEntity> findAllByAcceptedByIsNullAndIsClosedFalseAndIsDeletedFalse(Pageable pageable);

    Optional<OrderEntity> findByPublishedByUserIdAndAdvertisementId(Long userId, Long orderId);

    @Query("SELECT o FROM OrderEntity o " +
           "WHERE o.acceptedBy.organizationId = :organizationId " +
           "AND ((:isActive = true AND o.completedAt IS NULL) " +
           "OR (:isActive = false AND o.completedAt IS NOT NULL))")
    Page<OrderEntity> findByActiveStatus(Long organizationId, boolean isActive, Pageable pageable);

    @Query("SELECT o " +
           "FROM OrderEntity o " +
           "WHERE o.acceptedBy.organizationId = :organizationId " +
           "AND ((:isActive = true AND o.completedAt IS NULL) " +
           "OR (:isActive = false AND o.completedAt IS NOT NULL)) " +
           "AND ((:property = 'accepted' AND o.acceptedAt BETWEEN :dateFrom AND :dateTo) " +
           "OR (:property = 'deadline' AND o.deadlineAt BETWEEN :dateFrom AND :dateTo) " +
           "OR (:property = 'completed' AND o.completedAt BETWEEN :dateFrom AND :dateTo))")
    Page<OrderEntity> findByDateRange(Long organizationId, boolean isActive, String property, LocalDate dateFrom, LocalDate dateTo, Pageable pageable);

    @Query("SELECT DISTINCT o FROM OrderEntity o " +
           "LEFT JOIN o.acceptanceEntities ae " +
           "LEFT JOIN o.acceptedBy org " +
           "WHERE (org.organizationId = :organizationId AND o.status != :orderStatus) " +
           "OR (ae.organization.organizationId = :organizationId AND ae.requestedAt + 7 DAY >= CURRENT_DATE)")
    List<OrderEntity> findAllDashboardOrders(Long organizationId, OrderStatus orderStatus);

    @Query("SELECT o " +
           "FROM OrderEntity o " +
           "LEFT JOIN o.contractors co " +
           "WHERE (o.acceptedBy.organizationId = :organizationId AND co.userId = :employeeId) " +
           "AND ((:isActive = true AND o.completedAt IS NULL) " +
           "OR (:isActive = false AND o.completedAt IS NOT NULL))")
    Page<OrderEntity> findTasksByEmployeeId(Long employeeId, Long organizationId, boolean isActive, Pageable pageable);

    Optional<OrderEntity> findByAcceptedByOrganizationIdAndCompletedAtIsNullAndAdvertisementId(Long organizationId, Long orderId);

    @Query("SELECT new dev.yerokha.smarttale.dto.SearchItem(" +
           "o.advertisementId, " +
           "dev.yerokha.smarttale.enums.ContextType.ORDER, " +
           "o.title, " +
           "(SELECT i.imageUrl FROM Image i WHERE i IN elements(o.images) ORDER BY i.imageId ASC)" +
           ") " +
           "FROM OrderEntity o " +
           "WHERE (lower(o.title) LIKE %:query% " +
           "OR lower(o.description) LIKE %:query%) " +
           "AND ((:userId IS NULL AND o.isClosed = false) OR (o.publishedBy.userId = :userId)) " +
           "AND o.isDeleted = false")
    Page<SearchItem> findSearchedItemsJPQL(String query, Long userId, Pageable pageable);

    @Query("SELECT new dev.yerokha.smarttale.dto.SearchItem(" +
           "o.advertisementId, " +
           "dev.yerokha.smarttale.enums.ContextType.ORDER, " +
           "o.title, " +
           "(SELECT i.imageUrl FROM Image i WHERE i IN elements(o.images) ORDER BY i.imageId ASC)" +
           ") " +
           "FROM OrderEntity o " +
           "WHERE (lower(o.title) LIKE %:query% " +
           "OR lower(o.description) LIKE %:query%) " +
           "AND (o.acceptedBy.organizationId = :organizationId)")
    Page<SearchItem> findSearchedItemsJPQL(String query, Long organizationId, Pageable pageable, String org);
}







