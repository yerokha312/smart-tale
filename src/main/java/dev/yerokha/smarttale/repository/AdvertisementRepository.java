package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.dto.AdvertisementInterface;
import dev.yerokha.smarttale.dto.SearchItem;
import dev.yerokha.smarttale.entity.advertisement.Advertisement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {


    @Query("SELECT new dev.yerokha.smarttale.dto.AdvertisementDto(" +
           "CASE TYPE (a) " +
           "WHEN OrderEntity THEN 'order' " +
           "WHEN ProductEntity THEN 'product'" +
           "END, " +
           "a.advertisementId, " +
           "SUBSTRING(a.title, 1, 60), " +
           "SUBSTRING(a.description, 1, 120)," +
           "CASE TYPE (a) " +
           "WHEN OrderEntity THEN COALESCE((SELECT o.price FROM OrderEntity o WHERE a.advertisementId = o.advertisementId), 0)" +
           "WHEN ProductEntity THEN (SELECT p.price FROM ProductEntity p WHERE a.advertisementId = p.advertisementId) " +
           "END, " +
           "COALESCE((SELECT i.imageUrl FROM AdvertisementImage ai LEFT JOIN ai.image i WHERE ai.advertisement = a AND ai.index = 0), ''), " +
           "a.publishedAt, " +
           "CASE TYPE (a) " +
           "WHEN OrderEntity THEN CAST((SELECT COUNT (ae.acceptanceId) FROM OrderEntity o JOIN o.acceptanceEntities ae WHERE ae.order.advertisementId = a.advertisementId) AS INTEGER) " +
           "ELSE 0 " +
           "END, " +
           "a.isClosed" +
           ") " +
           "FROM Advertisement a " +
           "WHERE a.publishedBy.userId = :userId AND a.isDeleted = false")
    Page<AdvertisementInterface> findPersonalAds(Long userId, Pageable pageable);

    Optional<Advertisement> findByPublishedByUserIdAndAdvertisementIdAndIsDeletedFalse(Long userId, Long advertisementId);

    Optional<Advertisement> findByAdvertisementIdAndIsDeletedFalseAndIsClosedFalse(Long advertisementId);

    @Query("SELECT COUNT(ae.acceptanceId) = 0 " +
           "FROM OrderEntity o " +
           "JOIN o.acceptanceEntities ae " +
           "WHERE o.advertisementId = :orderId " +
           "AND ae.organization.organizationId = :orgId")
    boolean canAcceptOrder(Long orgId, Long orderId);

    @Query("SELECT COUNT(ae.applicationId) " +
           "FROM JobEntity j " +
           "JOIN ApplicationEntity ae " +
           "WHERE j.advertisementId = :jobId")
    int countApplicantsByJobId(Long jobId);

    @Modifying
    @Query("UPDATE Advertisement a SET a.views = a.views + 1 WHERE a.advertisementId = :advertisementId")
    void incrementViewsCount(Long advertisementId);

    @Query("SELECT new dev.yerokha.smarttale.dto.SearchItem(" +
           "a.advertisementId, " +
           "CASE TYPE(a) WHEN OrderEntity THEN dev.yerokha.smarttale.enums.ContextType.ORDER " +
           "WHEN ProductEntity THEN dev.yerokha.smarttale.enums.ContextType.PRODUCT " +
           "ELSE dev.yerokha.smarttale.enums.ContextType.ADVERTISEMENT END, " +
           "a.title, " +
           "COALESCE((SELECT i.imageUrl FROM AdvertisementImage ai LEFT JOIN Image i ON ai.image.imageId = i.imageId WHERE ai.advertisement.advertisementId = a.advertisementId ORDER BY ai.index ASC LIMIT 1), '')" +
           ") " +
           "FROM Advertisement a " +
           "WHERE (lower(a.title) LIKE %:query% OR lower(a.description) LIKE %:query%) " +
           "AND ((:userId IS NULL AND a.isClosed = false) OR (a.publishedBy.userId = :userId)) " +
           "AND a.isDeleted = false")
    Page<SearchItem> findSearchedItemsJPQL(@Param("query") String query, @Param("userId") Long userId, Pageable pageable);
}
