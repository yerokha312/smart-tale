package dev.yerokha.smarttale.repository;

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

    Page<Advertisement> findAllByPublishedByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);

    Optional<Advertisement> findByPublishedByUserIdAndAdvertisementIdAndIsDeletedFalse(Long userId, Long advertisementId);

    Optional<Advertisement> findByAdvertisementIdAndIsDeletedFalseAndIsClosedFalse(Long advertisementId);

    @Modifying
    @Query("UPDATE Advertisement a SET a.views = a.views + 1 WHERE a.advertisementId = :advertisementId")
    void incrementViewsCount(Long advertisementId);

    @Query("SELECT new dev.yerokha.smarttale.dto.SearchItem(" +
           "a.advertisementId, " +
           "CASE TYPE(a) WHEN OrderEntity THEN dev.yerokha.smarttale.enums.ContextType.ORDER " +
           "WHEN ProductEntity THEN dev.yerokha.smarttale.enums.ContextType.PRODUCT " +
           "ELSE dev.yerokha.smarttale.enums.ContextType.ADVERTISEMENT END, " +
           "a.title, " +
           "(SELECT i.imageUrl FROM Image i WHERE i IN elements(a.images) ORDER BY i.imageId ASC)" +
           ") " +
           "FROM Advertisement a " +
           "WHERE (lower(a.title) LIKE %:query% OR lower(a.description) LIKE %:query%) " +
           "AND ((:userId IS NULL AND a.isClosed = false) OR (a.publishedBy.userId = :userId)) " +
           "AND a.isDeleted = false")
    Page<SearchItem> findSearchedItemsJPQL(@Param("query") String query, @Param("userId") Long userId, Pageable pageable);
}
