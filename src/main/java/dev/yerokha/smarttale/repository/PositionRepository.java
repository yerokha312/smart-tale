package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.entity.user.PositionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<PositionEntity, Long> {

    Optional<PositionEntity> findByOrganizationOrganizationIdAndPositionId(Long organizationId, Long positionId);


    boolean existsByOrganizationOrganizationIdAndPositionId(Long organizationId, Long aLong);
}
