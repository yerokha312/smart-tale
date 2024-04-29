package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.entity.advertisement.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    Page<ProductEntity> findAllByPublishedByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);

    Optional<ProductEntity> findByPurchasedByUserIdAndAdvertisementId(Long userId, Long advId);

    Page<ProductEntity> findAllByPurchasedByIsNullAndIsClosedFalseAndIsDeletedFalse(Pageable pageable);

}
