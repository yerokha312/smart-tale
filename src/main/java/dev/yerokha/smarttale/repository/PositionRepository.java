package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.entity.user.PositionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<PositionEntity, Long> {

    Optional<PositionEntity> findByOrganizationOrganizationIdAndPositionId(Long organizationId, Long positionId);


    @Query("SELECT COUNT (p.positionId) > 0 " +
           "FROM PositionEntity p " +
           "WHERE p.positionId = :positionId " +
           "AND p.organization.organizationId = :organizationId")
    boolean existsByOrganizationOrganizationIdAndPositionId(Long organizationId, Long positionId);
}
