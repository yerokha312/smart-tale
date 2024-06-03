package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.dto.MarketCard;
import dev.yerokha.smarttale.entity.advertisement.JobEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface JobRepository extends JpaRepository<JobEntity, Long> {

    @Query("SELECT new dev.yerokha.smarttale.dto.MarketCard(" +
           "j.advertisementId, " +
           "j.publishedAt, " +
           "SUBSTRING(j.title, 1, 60), " +
           "SUBSTRING(j.description, 1, 120), " +
           "COALESCE(j.salary, 0), " +
           "COALESCE((SELECT i.imageUrl FROM Image i WHERE i MEMBER OF j.images ORDER BY i.imageUrl ASC), ''), " +
           "j.organization.organizationId, " +
           "j.organization.name, " +
           "COALESCE(orgImg.imageUrl, ''), " +
           "CASE WHEN j.organization.organizationId = :orgId THEN false ELSE true END" +
           ") " +
           "FROM JobEntity j " +
           "LEFT JOIN j.organization.image orgImg " +
           "WHERE j.isDeleted = false AND j.isClosed = false AND j.applicationDeadline >= CURRENT DATE")
    Page<MarketCard> findMarketJobs(Long orgId, Pageable pageable);
}
