package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.entity.user.PositionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PositionRepository extends JpaRepository<PositionEntity, Long> {
}