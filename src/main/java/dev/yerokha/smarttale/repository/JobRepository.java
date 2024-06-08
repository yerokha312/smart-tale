package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.dto.Card;
import dev.yerokha.smarttale.dto.JobSummary;
import dev.yerokha.smarttale.entity.advertisement.JobEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface JobRepository extends JpaRepository<JobEntity, Long> {

    @Query("SELECT new dev.yerokha.smarttale.dto.Card(" +
           "    j.advertisementId, " +
           "    j.publishedAt, " +
           "    SUBSTRING(j.title, 1, 60), " +
           "    SUBSTRING(j.description, 1, 120), " +
           "    COALESCE(j.salary, 0), " +
           "    COALESCE((" +
           "        SELECT i.imageUrl " +
           "        FROM AdvertisementImage ai " +
           "        LEFT JOIN ai.image i " +
           "        WHERE ai.advertisement = j AND ai.index = 0), ''), " +
           "    j.organization.organizationId, " +
           "    j.organization.name, " +
           "    COALESCE(orgImg.imageUrl, ''), " +
           "    CASE " +
           "        WHEN j.organization.organizationId = :orgId THEN false " +
           "        ELSE true " +
           "    END) " +
           "FROM JobEntity j " +
           "LEFT JOIN j.organization.image orgImg " +
           "WHERE j.isDeleted = false AND j.isClosed = false AND j.applicationDeadline >= CURRENT_DATE")
    Page<Card> findMarketJobs(Long orgId, Pageable pageable);

    @Query("SELECT new dev.yerokha.smarttale.dto.JobSummary(" +
           "    j.advertisementId, " +
           "    j.publishedAt, " +
           "    SUBSTRING(j.title, 1, 60), " +
           "    SUBSTRING(j.description, 1, 120), " +
           "    j.jobType, " +
           "    j.salary, " +
           "    COALESCE((" +
           "        SELECT i.imageUrl " +
           "        FROM AdvertisementImage ai " +
           "        LEFT JOIN ai.image i " +
           "        WHERE ai.advertisement = j AND ai.index = 0), ''), " +
           "    CAST((" +
           "        SELECT COUNT (a.applicationId) " +
           "        FROM j.applications a) AS INTEGER), " +
           "    j.isClosed) " +
           "FROM JobEntity j " +
           "WHERE j.organization.organizationId = :orgId AND j.isDeleted = false")
    Page<JobSummary> findAllByOrganizationId(Long orgId, Pageable pageable);

    Optional<JobEntity> findByOrganization_OrganizationIdAndAdvertisementIdAndIsDeletedFalse(Long orgId, Long jobId);

    @Modifying
    @Query("UPDATE JobEntity j " +
           "SET j.isClosed = true " +
           "WHERE j.organization.organizationId = :orgId AND j.advertisementId = :jobId")
    int setJobClosedTrueByOrganizationIdAndJobId(Long orgId, Long jobId);

    @Modifying
    @Query("UPDATE JobEntity j " +
           "SET j.isClosed = false " +
           "WHERE j.organization.organizationId = :orgId AND j.advertisementId = :jobId")
    int setJobClosedFalseByOrganizationIdAndJobId(Long orgId, Long jobId);

    @Modifying
    @Query("UPDATE JobEntity j " +
           "SET j.isDeleted = true " +
           "WHERE j.organization.organizationId = :orgId AND j.advertisementId = :jobId")
    int setJobDeletedByOrganizationIdAndJobId(Long orgId, Long jobId);

    boolean existsByAdvertisementIdAndOrganization_OrganizationId(Long advertisementId, Long orgId);
}
