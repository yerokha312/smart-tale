package dev.yerokha.smarttale.entity.advertisement;

import dev.yerokha.smarttale.entity.user.OrganizationEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;
import java.util.Objects;

@Data
@Entity
@Table(name = "acceptance_requests")
public class AcceptanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "acceptance_id")
    private Long acceptanceId;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private OrganizationEntity organization;

    @Column(name = "requested_at")
    private LocalDate requestedAt;

    public AcceptanceEntity() {
    }

    public AcceptanceEntity(OrderEntity order, OrganizationEntity organization, LocalDate requestedAt) {
        this.order = order;
        this.organization = organization;
        this.requestedAt = requestedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AcceptanceEntity that = (AcceptanceEntity) o;
        return Objects.equals(order, that.order) && Objects.equals(organization, that.organization);
    }

    @Override
    public int hashCode() {
        return Objects.hash(order, organization);
    }
}
