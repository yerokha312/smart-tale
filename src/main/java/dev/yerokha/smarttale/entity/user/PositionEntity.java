package dev.yerokha.smarttale.entity.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

import java.util.Objects;
import java.util.Set;

@Data
@Entity
@Table(name = "positions")
public class PositionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "position_id")
    private Long positionId;

    @Column(name = "title")
    private String title;

    @Column(name = "hierarchy", columnDefinition = "integer default 0")
    private int hierarchy = 0;

    @Column(name = "authorities", columnDefinition = "integer default 0")
    private int authorities = 0;

    @ToString.Exclude
    @OneToMany(mappedBy = "position")
    private Set<UserDetailsEntity> employees;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private OrganizationEntity organization;

    public PositionEntity() {
    }

    public PositionEntity(String title, Integer hierarchy, Integer authorities, OrganizationEntity organization) {
        this.title = title;
        this.hierarchy = hierarchy;
        this.authorities = authorities;
        this.organization = organization;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PositionEntity that = (PositionEntity) o;
        return Objects.equals(positionId, that.positionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(positionId);
    }
}
