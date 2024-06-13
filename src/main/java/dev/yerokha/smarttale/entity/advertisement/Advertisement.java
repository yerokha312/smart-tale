package dev.yerokha.smarttale.entity.advertisement;

import dev.yerokha.smarttale.entity.AdvertisementImage;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.enums.ContactInfo;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Getter
@Setter
@Entity
@Table(name = "abstract_advertisements")
@Inheritance(strategy = InheritanceType.JOINED)
public class Advertisement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "advertisement_id")
    private Long advertisementId;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @ManyToOne
    @JoinColumn(name = "published_by")
    private UserDetailsEntity publishedBy;

    @Column(name = "title", nullable = false, length = 250)
    private String title;

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @OneToMany(mappedBy = "advertisement", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<AdvertisementImage> advertisementImages = new ArrayList<>();

    @Column(name = "contact_information")
    @Enumerated(EnumType.ORDINAL)
    private ContactInfo contactInfo;

    @Column(name = "views", columnDefinition = "bigint default 0")
    private long views;

    @Column(name = "is_deleted", columnDefinition = "boolean default false")
    private boolean isDeleted = false;

    @Column(name = "is_closed", columnDefinition = "boolean default false")
    private boolean isClosed = false;

    public Advertisement() {
    }

    public Advertisement(LocalDateTime publishedAt,
                         UserDetailsEntity publishedBy,
                         String title,
                         String description,
                         List<AdvertisementImage> advertisementImages,
                         ContactInfo contactInfo) {
        this.publishedAt = publishedAt;
        this.publishedBy = publishedBy;
        this.title = title;
        this.description = description;
        this.advertisementImages = advertisementImages;
        this.contactInfo = contactInfo;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Advertisement that)) return false;
        return Objects.equals(advertisementId, that.advertisementId) && Objects.equals(publishedAt, that.publishedAt) && Objects.equals(publishedBy, that.publishedBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(advertisementId, publishedAt, publishedBy);
    }

    @Override
    public String toString() {
        return "Advertisement{" +
               "advertisementId=" + advertisementId +
               ", publishedAt=" + publishedAt +
               ", publishedBy=" + publishedBy +
               ", title='" + title + '\'' +
               ", description='" + description + '\'' +
               ", contactInfo=" + contactInfo +
               ", views=" + views +
               ", isDeleted=" + isDeleted +
               ", isClosed=" + isClosed +
               '}';
    }
}
