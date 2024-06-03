package dev.yerokha.smarttale.entity.advertisement;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "products")
@OnDelete(action = OnDeleteAction.CASCADE)
public class ProductEntity extends Advertisement {

    @Column(name = "price")
    private BigDecimal price; //sort

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseEntity> purchases = new ArrayList<>();

    @Transient
    private LocalDateTime purchasedAt;

    public void addPurchase(PurchaseEntity purchase) {
        purchases.add(purchase);

    }
}
