package dev.yerokha.smarttale.entity.user;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import dev.yerokha.smarttale.entity.Image;
import dev.yerokha.smarttale.entity.advertisement.OrderEntity;
import dev.yerokha.smarttale.entity.advertisement.ProductEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "user_details")
public class UserDetailsEntity {

    @Id
    private Long userId;

    @JsonManagedReference
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @MapsId
    @JoinColumn(name = "details_id")
    private UserEntity user;

    @OneToMany(mappedBy = "publishedBy")
    private List<OrderEntity> orders;

    @OneToMany(mappedBy = "publishedBy")
    private List<ProductEntity> products;

    @OneToMany(mappedBy = "purchasedBy")
    private List<ProductEntity> purchases;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private OrganizationEntity organization;

    @OneToMany(mappedBy = "acceptedBy")
    private List<OrderEntity> acceptedOrders;

    @ManyToOne
    @JoinColumn(name = "image_id")
    private Image image;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @Column(name = "is_subscribed", columnDefinition = "boolean default false")
    private boolean isSubscribed = false;

    @Column(name = "subscription_start_date")
    private LocalDate subscriptionStartDate;

    @Column(name = "subscription_end_date")
    private LocalDate subscriptionEndDate;
}
