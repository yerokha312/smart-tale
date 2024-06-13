package dev.yerokha.smarttale.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import dev.yerokha.smarttale.entity.Image;
import dev.yerokha.smarttale.entity.advertisement.JobApplicationEntity;
import dev.yerokha.smarttale.entity.advertisement.OrderEntity;
import dev.yerokha.smarttale.entity.advertisement.ProductEntity;
import dev.yerokha.smarttale.entity.advertisement.PurchaseEntity;
import dev.yerokha.smarttale.enums.ContactInfo;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "user_details")
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDetailsEntity {

    @Id
    private Long userId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "visible_contacts", columnDefinition = "varchar(100) default 'EMAIL_PHONE'")
    private ContactInfo visibleContacts;

    @ManyToOne
    @JoinColumn(name = "position_id")
    private PositionEntity position;

    @OneToMany(mappedBy = "invitee", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Set<InvitationEntity> invitations;

    @JsonManagedReference
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @MapsId
    @JoinColumn(name = "details_id")
    private UserEntity user;

    @OneToMany(mappedBy = "publishedBy")
    private List<OrderEntity> orders;

    @OneToMany(mappedBy = "publishedBy")
    private List<ProductEntity> products;

    @OneToMany(mappedBy = "purchasedBy")
    private List<PurchaseEntity> purchases = new ArrayList<>();

    @OneToMany(mappedBy = "applicant")
    private List<JobApplicationEntity> applications;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private OrganizationEntity organization;

    @ManyToMany
    @JoinTable(
            name = "task_employee_junction",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "task_id")
    )
    private List<OrderEntity> assignedTasks;

    @Column(name = "active_orders_count", columnDefinition = "int default 0")
    private int activeOrdersCount;

    @ManyToOne
    @JoinColumn(name = "image_id")
    private Image image;

    @JsonIgnore
    @Column(name = "registered_at")
    private LocalDateTime registeredAt = LocalDateTime.now();

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @Column(name = "is_subscribed", columnDefinition = "boolean default false")
    private boolean isSubscribed = false;

    @Column(name = "subscription_start_date")
    private LocalDate subscriptionStartDate;

    @Column(name = "subscription_end_date")
    private LocalDate subscriptionEndDate;

    public UserDetailsEntity() {
    }

    public UserDetailsEntity(String firstName, String lastName, String middleName, String email, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return (this.lastName == null ? "" : this.lastName + " " + this.firstName + " " +
                                             (this.middleName == null ? "" : this.middleName)).trim();
    }

    public void addAssignedTask(OrderEntity task) {
        if (this.assignedTasks == null) {
            this.assignedTasks = new ArrayList<>();
        }

        this.assignedTasks.add(task);
    }

    public void removeAssignedTask(OrderEntity task) {
        if (this.assignedTasks == null || this.assignedTasks.isEmpty()) {
            throw new IllegalArgumentException("Assigned tasks list is empty");
        }

        this.assignedTasks.remove(task);
    }

    public String getAvatarUrl() {
        return this.image == null ? "" : this.image.getImageUrl();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof UserDetailsEntity that)) return false;
        return Objects.equals(userId, that.userId) && Objects.equals(registeredAt, that.registeredAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, registeredAt);
    }

    @Override
    public String toString() {
        return "UserDetailsEntity{" +
               "userId=" + userId +
               ", firstName='" + firstName + '\'' +
               ", lastName='" + lastName + '\'' +
               ", middleName='" + middleName + '\'' +
               ", email='" + email + '\'' +
               ", phoneNumber='" + phoneNumber + '\'' +
               ", visibleContacts=" + visibleContacts +
               ", position=" + position +
               ", registeredAt=" + registeredAt +
               '}';
    }
}
