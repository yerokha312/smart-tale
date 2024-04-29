package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.entity.advertisement.Advertisement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {

    Page<Advertisement> findAllByPublishedByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);
    Optional<Advertisement> findByPublishedByUserIdAndAdvertisementId(Long userId, Long advertisementId);

    @Query("SELECT a " +
            "FROM Advertisement a " +
            "LEFT JOIN OrderEntity o ON a.advertisementId = o.advertisementId " +
            "LEFT JOIN ProductEntity p ON a.advertisementId = p.advertisementId " +
            "WHERE (TYPE(a) = OrderEntity AND o.completedAt IS NOT NULL AND a.publishedBy.userId = :userId) " +
            "OR (TYPE(a) = ProductEntity AND a.purchasedAt IS NOT NULL AND p.purchasedBy.userId = :userId) " +
            "ORDER BY a.purchasedAt DESC")
    Page<Advertisement> findUserPurchases(@Param("userId") Long userId, Pageable pageable);

}
