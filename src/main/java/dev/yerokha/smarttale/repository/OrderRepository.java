package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.entity.advertisement.OrderEntity;
import dev.yerokha.smarttale.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    Page<OrderEntity> findAllByPublishedByUserId(Long userId, Pageable pageable);

    Page<OrderEntity> findAllByAcceptedByUserIdAndStatusNotIn(Long userId, List<OrderStatus> orderStatuses, Pageable pageable);

    Optional<OrderEntity> findByAcceptedByUserIdAndAdvertisementId(Long userId, Long orderId);

    Page<OrderEntity> findAllByAcceptedByUserIdInAndStatusNotIn(Set<Long> employeeIds, List<OrderStatus> orderStatuses, Pageable pageable);

}
