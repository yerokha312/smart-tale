package dev.yerokha.smarttale.entity.user;

import dev.yerokha.smarttale.entity.Image;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Entity
@Table(name = "organizations")
public class OrganizationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    @OneToOne
    @JoinColumn(name = "image_id")
    private Image image;

    @Column(name = "founded_at")
    private LocalDateTime foundedAt;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    @OneToOne
    @JoinColumn(name = "owner_id")
    private UserDetailsEntity owner;

    @OneToMany(mappedBy = "organization")
    private Set<UserDetailsEntity> employees;

    @Column(name = "is_deleted")
    private boolean isDeleted;

}
