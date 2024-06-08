package dev.yerokha.smarttale.entity.advertisement;

import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.enums.PurchaseStatus;
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

import java.math.BigDecimal;
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

    @Column(name = "quantity", columnDefinition = "numeric(38,2) default 0")
    private int quantity;

    @Column(name = "total_price", columnDefinition = "numeric(38,2) default 0")
    private BigDecimal totalPrice;

    @Column(name = "purchased_at")
    private LocalDateTime purchasedAt;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private PurchaseStatus status = PurchaseStatus.PENDING;

    @Column(name = "status_changed_at")
    private LocalDateTime statusChangedAt;

    public PurchaseEntity() {
    }

    public PurchaseEntity(UserDetailsEntity purchasedBy,
                          ProductEntity product,
                          int quantity,
                          BigDecimal totalPrice,
                          LocalDateTime purchasedAt,
                          PurchaseStatus status,
                          LocalDateTime statusChangedAt) {
        this.purchasedBy = purchasedBy;
        this.product = product;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.purchasedAt = purchasedAt;
        this.status = status;
        this.statusChangedAt = statusChangedAt;
    }
}
