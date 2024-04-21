package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.entity.advertisement.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    Page<OrderEntity> findAllByPublishedByUserId(Long userId, Pageable pageable);
}
