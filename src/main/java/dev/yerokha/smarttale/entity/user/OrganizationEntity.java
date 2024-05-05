package dev.yerokha.smarttale.entity.user;

import dev.yerokha.smarttale.entity.Image;
import dev.yerokha.smarttale.entity.advertisement.OrderEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Getter
@Setter
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
    private LocalDate foundedAt;

    @Column(name = "registered_at")
    private LocalDate registeredAt;

    @OneToOne
    @JoinColumn(name = "owner_id")
    private UserDetailsEntity owner;

    @OneToMany(mappedBy = "organization")
    private Set<UserDetailsEntity> employees;

    @OneToMany(mappedBy = "acceptedBy")
    private List<OrderEntity> acceptedOrders;

    @OneToMany(mappedBy = "organization")
    private List<PositionEntity> positions;

    @OneToMany(mappedBy = "organization")
    private Set<InvitationEntity> invitations;

    @Column(name = "is_deleted", columnDefinition = "boolean default false")
    private boolean isDeleted = false;

}
