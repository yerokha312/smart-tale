package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.entity.advertisement.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    Page<OrderEntity> findAllByPublishedByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);

    Page<OrderEntity> findAllByPublishedByUserIdAndCompletedAtNotNull(Long userId, Pageable pageable);

    Page<OrderEntity> findAllByPublishedByUserIdAndAcceptedByIsNotNullAndCompletedAtIsNull(Long userId, Pageable pageable);

    Page<OrderEntity> findAllByAcceptedByIsNullAndIsClosedFalseAndIsDeletedFalse(Pageable pageable);

    Optional<OrderEntity> findByPublishedByUserIdAndAdvertisementId(Long userId, Long orderId);

    Page<OrderEntity> findAllByAcceptedByOrganizationIdAndCompletedAtIsNull(Long organizationId, Pageable pageable);

    Page<OrderEntity> findAllByAcceptedByOrganizationIdAndCompletedAtIsNotNull(Long organizationId, Pageable pageable);
}
