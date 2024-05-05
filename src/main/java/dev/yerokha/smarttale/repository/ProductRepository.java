package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.entity.advertisement.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    Page<ProductEntity> findAllByPublishedByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);


    Page<ProductEntity> findAllByIsClosedFalseAndIsDeletedFalse(Pageable pageable);


}
