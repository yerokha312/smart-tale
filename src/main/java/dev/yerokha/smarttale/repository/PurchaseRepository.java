package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.entity.advertisement.ProductEntity;
import dev.yerokha.smarttale.entity.advertisement.PurchaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<PurchaseEntity, Long> {

    @Query("SELECT p.product FROM PurchaseEntity p WHERE p.purchasedBy = :userId ORDER BY p.purchasedAt DESC")
    Page<ProductEntity> findAllProductsPurchasedByUser(Long userId, Pageable pageable);

    Page<PurchaseEntity> findAllByPurchasedByUserId(Long userId, Pageable pageable);

    @Query("SELECT p.product FROM PurchaseEntity p WHERE p.purchasedBy = :userId AND p.product = :productId")
    Optional<ProductEntity> findPurchasedProductByUserIdAndProductId(Long userId, Long productId);

}
