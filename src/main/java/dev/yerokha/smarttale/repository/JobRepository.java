package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.entity.advertisement.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<JobEntity, Long> {
}
