package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.entity.advertisement.AcceptanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AcceptanceRepository extends JpaRepository<AcceptanceEntity, Long> {
}
