package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.dto.SearchItem;
import dev.yerokha.smarttale.entity.advertisement.PurchaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseRepository extends JpaRepository<PurchaseEntity, Long> {

    Page<PurchaseEntity> findAllByPurchasedByUserId(Long userId, Pageable pageable);

    @Query("SELECT new dev.yerokha.smarttale.dto.SearchItem(" +
           "p.purchaseId, " +
           "dev.yerokha.smarttale.enums.ContextType.PURCHASE, " +
           "p.product.title, " +
           "(SELECT i.imageUrl FROM Image i WHERE i IN elements(p.product.images) ORDER BY i.imageId ASC)" +
           ") " +
           "FROM PurchaseEntity p " +
           "WHERE (lower(p.product.title) LIKE %:query% " +
           "OR lower(p.product.description) LIKE %:query%) " +
           "AND p.purchasedBy.userId = :userId")
    Page<SearchItem> findSearchedItemsJPQL(String query, Long userId, Pageable pageable);
}
