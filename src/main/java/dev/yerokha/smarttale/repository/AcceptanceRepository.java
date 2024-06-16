package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.entity.advertisement.AcceptanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;


@Repository
public interface AcceptanceRepository extends JpaRepository<AcceptanceEntity, Long> {

    boolean existsByOrganization_OrganizationIdAndOrder_AdvertisementId(Long organizationId, Long orderId);

    @Modifying
    void deleteByOrder_AdvertisementIdAndOrganization_OrganizationId(Long orderId, Long organizationId);
}
