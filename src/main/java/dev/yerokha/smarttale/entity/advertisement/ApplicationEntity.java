package dev.yerokha.smarttale.entity.advertisement;

import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.enums.ApplicationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "applications")
public class ApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Long applicationId;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private JobEntity job;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserDetailsEntity applicant;

    @Column(name = "application_date")
    private LocalDateTime applicationDate;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    public ApplicationEntity() {
    }

    public ApplicationEntity(JobEntity job, UserDetailsEntity applicant, LocalDateTime applicationDate, ApplicationStatus status) {
        this.job = job;
        this.applicant = applicant;
        this.applicationDate = applicationDate;
        this.status = status;
    }
}
