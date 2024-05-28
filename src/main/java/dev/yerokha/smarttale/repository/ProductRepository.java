package dev.yerokha.smarttale.repository;

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


    Page<ProductEntity> findAllByIsClosedFalseAndIsDeletedFalse(Pageable pageable);


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
