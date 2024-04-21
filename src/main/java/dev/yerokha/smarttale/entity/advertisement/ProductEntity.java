package dev.yerokha.smarttale.entity.advertisement;

import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "products")
public class ProductEntity extends Advertisement {

    @ManyToOne
    @JoinColumn(name = "purchased_by")
    private UserDetailsEntity purchasedBy;

    @Column(name = "purchased_at")
    private LocalDateTime purchasedAt;

}
