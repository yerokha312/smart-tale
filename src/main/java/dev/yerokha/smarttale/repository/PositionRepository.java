package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.entity.user.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {

    Optional<Position> findByOrganizationOrganizationIdAndTitle(Long orgId, String title);
}
