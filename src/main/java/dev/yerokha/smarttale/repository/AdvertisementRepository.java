package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.dto.AdvertisementDto;
import dev.yerokha.smarttale.dto.SearchItem;
import dev.yerokha.smarttale.entity.AdvertisementImage;
import dev.yerokha.smarttale.entity.advertisement.Advertisement;
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
public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {

    @Query("SELECT COUNT(o) > 0 " +
           "FROM OrderEntity o " +
           "WHERE o.advertisementId = :advertisementId " +
           "AND o.publishedBy.userId = :userId " +
           "AND o.acceptedAt IS NOT NULL")
    boolean existsAcceptedOrder(Long advertisementId, Long userId);

    @Modifying
    @Query("UPDATE Advertisement a " +
           "SET a.isDeleted = true " +
           "WHERE a.advertisementId = :advertisementId AND a.publishedBy.userId = :userId")
    int setDeletedByAdvertisementIdAndUserId(Long advertisementId, Long userId);

    @Query("SELECT new dev.yerokha.smarttale.dto.AdvertisementDto(" +
           "    CASE TYPE (a) " +
           "        WHEN OrderEntity THEN dev.yerokha.smarttale.enums.PersonalAdvertisementType.ORDER " +
           "        ELSE dev.yerokha.smarttale.enums.PersonalAdvertisementType.PRODUCT END, " +
           "    a.advertisementId, " +
           "    SUBSTRING(a.title, 1, 60), " +
           "    SUBSTRING(a.description, 1, 120), " +
           "    CASE WHEN TYPE(a) = OrderEntity THEN COALESCE(o.price, 0) " +
           "         ELSE p.price " +
           "    END, " +
           "    CASE WHEN TYPE(a) = ProductEntity THEN p.quantity " +
           "         ELSE 0 " +
           "    END, " +
           "    COALESCE(i.imageUrl, ''), " +
           "    a.publishedAt, " +
           "    CASE WHEN TYPE(a) = OrderEntity THEN CAST(COUNT(ae.acceptanceId) AS INTEGER) " +
           "         ELSE 0 " +
           "    END, " +
           "    a.isClosed) " +
           "FROM Advertisement a " +
           "LEFT JOIN OrderEntity o ON a.advertisementId = o.advertisementId " +
           "LEFT JOIN ProductEntity p ON a.advertisementId = p.advertisementId " +
           "LEFT JOIN AdvertisementImage ai ON ai.advertisement = a AND ai.index = 0 " +
           "LEFT JOIN ai.image i " +
           "LEFT JOIN o.acceptanceEntities ae " +
           "WHERE a.publishedBy.userId = :userId AND a.isDeleted = false " +
           "AND TYPE(a) NOT IN (JobEntity) " +
           "GROUP BY a, o, p, i.imageUrl")
    Page<AdvertisementDto> findPersonalAds(Long userId, Pageable pageable);

    @Query("SELECT a " +
           "FROM Advertisement a " +
           "WHERE a.isClosed = false AND a.isDeleted = false " +
           "AND a.publishedBy.user.isDeleted = false " +
           "AND a.advertisementId = :advertisementId")
    Optional<Advertisement> findMarketAdvertisement(Long advertisementId);

    @Query("SELECT COUNT(ae.acceptanceId) = 0 " +
           "FROM OrderEntity o " +
           "JOIN o.acceptanceEntities ae " +
           "WHERE o.advertisementId = :orderId AND ae.organization.organizationId = :orgId")
    boolean canAcceptOrder(Long orgId, Long orderId);

    @Query("SELECT DISTINCT COUNT(jae.applicant.userId) " +
           "FROM JobEntity j " +
           "JOIN j.applications jae " +
           "WHERE j.advertisementId = :jobId")
    int countApplicantsByJobId(Long jobId);

    @Modifying
    @Query("UPDATE Advertisement a " +
           "SET a.views = a.views + 1 " +
           "WHERE a.advertisementId = :advertisementId")
    void incrementViewsCount(Long advertisementId);

    @Query("SELECT new dev.yerokha.smarttale.dto.SearchItem(" +
           "a.advertisementId, " +
           "CASE TYPE(a) " +
           "    WHEN OrderEntity THEN dev.yerokha.smarttale.enums.ContextType.ORDER " +
           "    WHEN ProductEntity THEN dev.yerokha.smarttale.enums.ContextType.PRODUCT " +
           "    ELSE dev.yerokha.smarttale.enums.ContextType.ADVERTISEMENT " +
           "END, " +
           "a.title, " +
           "COALESCE((" +
           "    SELECT i.imageUrl " +
           "    FROM AdvertisementImage ai " +
           "    LEFT JOIN ai.image i " +
           "    WHERE ai.advertisement = a AND ai.index = 0), '')) " +
           "FROM Advertisement a " +
           "WHERE (lower(a.title) LIKE %:query% OR lower(a.description) LIKE %:query%) " +
           "AND ((:userId IS NULL AND a.isClosed = false) OR (a.publishedBy.userId = :userId)) " +
           "AND a.isDeleted = false")
    Page<SearchItem> findSearchedItemsJPQL(@Param("query") String query, @Param("userId") Long userId, Pageable pageable);

    @Modifying
    @Query("UPDATE Advertisement a " +
           "SET a.isClosed = false " +
           "WHERE a.advertisementId = :advertisementId AND a.publishedBy.userId = :userId")
    int setClosedFalseByAdvertisementIdAndUserId(Long advertisementId, Long userId);

    @Modifying
    @Query("UPDATE Advertisement a " +
           "SET a.isClosed = true " +
           "WHERE a.advertisementId = :advertisementId AND a.publishedBy.userId = :userId")
    int setClosedTrueByAdvertisementIdAndUserId(Long advertisementId, Long userId);

    @Query("SELECT ai " +
           "FROM AdvertisementImage ai " +
           "LEFT JOIN FETCH ai.image " +
           "WHERE ai.advertisement.advertisementId = :advertisementId " +
           "ORDER BY ai.index")
    List<AdvertisementImage> findAdvertisementImages(Long advertisementId);

    @Query("SELECT a FROM Advertisement a " +
           "WHERE a.publishedBy.userId = :userId AND a.advertisementId = :advertisementId " +
           "AND a.isDeleted = false AND TYPE (a) NOT IN (JobEntity)")
    Optional<Advertisement> findByUserIdAndAdIdAndDeletedFalse(Long userId, Long advertisementId);
}
