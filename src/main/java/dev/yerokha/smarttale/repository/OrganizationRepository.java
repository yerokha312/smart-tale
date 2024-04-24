package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.entity.user.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, Long> {

    Optional<OrganizationEntity> findByOwnerUserId(Long userId);

}
