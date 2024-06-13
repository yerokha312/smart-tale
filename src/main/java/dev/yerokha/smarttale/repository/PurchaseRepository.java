package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.dto.Purchase;
import dev.yerokha.smarttale.dto.PurchaseSummary;
import dev.yerokha.smarttale.dto.SearchItem;
import dev.yerokha.smarttale.entity.advertisement.PurchaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<PurchaseEntity, Long> {

    @Query("SELECT new dev.yerokha.smarttale.dto.Purchase(" +
           "    p.purchaseId, " +
           "    p.purchasedAt, " +
           "    p.status, " +
           "    p.statusChangedAt, " +
           "    pp.advertisementId, " +
           "    pp.title, " +
           "    pp.description, " +
           "    p.quantity, " +
           "    pp.price, " +
           "    p.totalPrice, " +
           "    (SELECT i.imageUrl " +
           "        FROM AdvertisementImage ai " +
           "        LEFT JOIN ai.image i " +
           "        WHERE ai.advertisement = pp AND ai.index = 0), " +
           "    pp.publishedBy.userId, " +
           "    CONCAT(pp.publishedBy.lastName, ' ', pp.publishedBy.firstName), " +
           "    COALESCE(pubImg.imageUrl, ''), " +
           "    pp.publishedBy.phoneNumber, " +
           "    pp.publishedBy.email, " +
           "    (pp.isDeleted AND pp.isClosed)) " +
           "FROM PurchaseEntity p " +
           "JOIN p.product pp " +
           "LEFT JOIN pp.publishedBy.image pubImg " +
           "WHERE p.purchasedBy.userId = :userId AND p.purchaseId = :purchaseId " +
           "AND pp.isDeleted = false AND pp.isClosed = false " +
           "AND pp.quantity > 0")
    Optional<Purchase> findByPurchaseIdAndUserId(Long purchaseId, Long userId);

    @Query("SELECT new dev.yerokha.smarttale.dto.PurchaseSummary(" +
           "    p.purchaseId, " +
           "    p.purchasedAt, " +
           "    p.status, " +
           "    pp.advertisementId, " +
           "    pp.title, " +
           "    pp.description, " +
           "    p.totalPrice, " +
           "    COALESCE((" +
           "        SELECT i.imageUrl " +
           "        FROM AdvertisementImage ai " +
           "        LEFT JOIN ai.image i " +
           "        WHERE ai.advertisement = pp AND ai.index = 0), ''), " +
           "    pp.publishedBy.userId, " +
           "    CONCAT(pp.publishedBy.lastName, ' ', pp.publishedBy.firstName), " +
           "    COALESCE(pubImg.imageUrl, ''), " +
           "    (pp.isDeleted AND pp.isClosed)) " +
           "FROM PurchaseEntity p " +
           "JOIN p.product pp " +
           "LEFT JOIN pp.publishedBy.image pubImg " +
           "WHERE p.purchasedBy.userId = :userId")
    Page<PurchaseSummary> findAllByPurchasedUserId(Long userId, Pageable pageable);

    @Query("SELECT new dev.yerokha.smarttale.dto.SearchItem(" +
           "    p.purchaseId, " +
           "    dev.yerokha.smarttale.enums.ContextType.PURCHASE, " +
           "    p.product.title, " +
           "    COALESCE((" +
           "        SELECT i.imageUrl " +
           "        FROM AdvertisementImage ai " +
           "        LEFT JOIN ai.image i " +
           "        WHERE ai.advertisement = p.product AND ai.index = 0), '')) " +
           "FROM PurchaseEntity p " +
           "WHERE (lower(p.product.title) LIKE %:query% " +
           "OR lower(p.product.description) LIKE %:query%) " +
           "AND p.purchasedBy.userId = :userId")
    Page<SearchItem> findSearchedItemsJPQL(String query, Long userId, Pageable pageable);
}
