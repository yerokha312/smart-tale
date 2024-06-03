package dev.yerokha.smarttale.entity.advertisement;

import dev.yerokha.smarttale.entity.Image;
import dev.yerokha.smarttale.entity.user.OrganizationEntity;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.enums.ContactInfo;
import dev.yerokha.smarttale.enums.JobType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "jobs")
public class JobEntity extends Advertisement {

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private OrganizationEntity organization;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApplicationEntity> applications = new ArrayList<>();

    @Column(name = "job_type")
    @Enumerated(EnumType.STRING)
    private JobType jobType;

    @Column(name = "location")
    private String location;

    @Column(name = "salary")
    private BigDecimal salary;

    @Column(name = "application_deadline")
    private LocalDate applicationDeadline;

    public JobEntity() {
    }

    public JobEntity(LocalDateTime publishedAt,
                     UserDetailsEntity publishedBy,
                     String title,
                     String description,
                     List<Image> images,
                     ContactInfo contactInfo,
                     OrganizationEntity organization,
                     JobType jobType,
                     String location,
                     BigDecimal salary,
                     LocalDate applicationDeadline) {
        super(publishedAt, publishedBy, title, description, images, contactInfo);
        this.organization = organization;
        this.jobType = jobType;
        this.location = location;
        this.salary = salary;
        this.applicationDeadline = applicationDeadline;
    }
}
