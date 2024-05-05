package dev.yerokha.smarttale.entity.advertisement;

import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "purchases")
public class PurchaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_id")
    private Long purchaseId;

    @ManyToOne
    @JoinColumn(name = "purchased_by")
    private UserDetailsEntity purchasedBy;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    @Column(name = "purchased_at")
    private LocalDateTime purchasedAt;

    public PurchaseEntity() {
    }

    public PurchaseEntity(UserDetailsEntity purchasedBy, ProductEntity product, LocalDateTime purchasedAt) {
        this.purchasedBy = purchasedBy;
        this.product = product;
        this.purchasedAt = purchasedAt;
    }
}
