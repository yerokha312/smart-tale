package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.dto.MarketCard;
import dev.yerokha.smarttale.dto.SearchItem;
import dev.yerokha.smarttale.entity.advertisement.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    Page<ProductEntity> findAllByPublishedByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);



    @Query("SELECT new dev.yerokha.smarttale.dto.MarketCard(" +
           "p.advertisementId, " +
           "p.publishedAt, " +
           "SUBSTRING(p.title, 1, 60), " +
           "SUBSTRING(p.description, 1, 120), " +
           "COALESCE(p.price, 0), " +
           "COALESCE((SELECT i.imageUrl FROM Image i WHERE i MEMBER OF p.images ORDER BY i.imageId ASC), ''), " +
           "p.publishedBy.userId, " +
           "CONCAT(p.publishedBy.lastName, ' ', p.publishedBy.firstName), " +
           "COALESCE(pubImg.imageUrl, ''), " +
           "CASE WHEN p.publishedBy.userId = :userId THEN false ELSE true END" +
           ") " +
           "FROM ProductEntity p " +
           "LEFT JOIN p.images i ON i.imageId = (SELECT MIN(i2.imageId) FROM p.images i2) " +
           "LEFT JOIN p.publishedBy.image pubImg " +
           "WHERE p.isDeleted = false AND p.isClosed = false")
    Page<MarketCard> findMarketProducts(Long userId, Pageable pageable);

    @Query("SELECT new dev.yerokha.smarttale.dto.SearchItem(" +
           "p.advertisementId, " +
           "dev.yerokha.smarttale.enums.ContextType.PRODUCT, " +
           "p.title, " +
           "(SELECT i.imageUrl FROM Image i WHERE i IN elements(p.images) ORDER BY i.imageId ASC)" +
           ") " +
           "FROM ProductEntity p " +
           "WHERE (lower(p.title) LIKE %:query% " +
           "OR lower(p.description) LIKE %:query%) " +
           "AND ((:userId IS NULL AND p.isClosed = false) OR (p.publishedBy.userId = :userId)) " +
           "AND p.isDeleted = false")
    Page<SearchItem> findSearchedItemsJPQL(@Param("query") String query, @Param("userId") Long userId, Pageable pageable);
}
