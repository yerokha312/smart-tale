package dev.yerokha.smarttale.entity.user;

import dev.yerokha.smarttale.entity.Image;
import dev.yerokha.smarttale.entity.advertisement.EquipmentEntity;
import dev.yerokha.smarttale.entity.advertisement.OrderEntity;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "user_details")
public class UserDetailsEntity {

    @Id
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "details_id")
    private AppUserEntity user;

    @OneToMany(mappedBy = "postedUser")
    private List<OrderEntity> orders;

    @OneToMany(mappedBy = "postedUser")
    private List<EquipmentEntity> equipments;

    @OneToMany(mappedBy = "purchasedUser")
    private List<EquipmentEntity> purchases;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private OrganizationEntity organization;

    @ManyToOne
    @JoinColumn(name = "image_id")
    private Image image;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @Column(name = "is_subscribed")
    private boolean isSubscribed;

    @Column(name = "subscription_start_date")
    private LocalDate subscriptionStartDate;

    @Column(name = "subscription_end_date")
    private LocalDate subscriptionEndDate;
}
