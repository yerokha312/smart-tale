package dev.yerokha.smarttale.entity.advertisement;

import dev.yerokha.smarttale.entity.Image;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.enums.ContactInfo;
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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
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
    private String title; //sort

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @Column(name = "price")
    private BigDecimal price; //sort

    @ManyToMany
    @JoinTable(
            name = "advertisement_image_junction",
            joinColumns = @JoinColumn(name = "advertisement_id"),
//            uniqueConstraints = @UniqueConstraint(columnNames = {"advertisement_id", "image_id"}),
            inverseJoinColumns = @JoinColumn(name = "image_id")
    )
    @Size(max = 5)
    private List<Image> images = new ArrayList<>();

    @Column(name = "contact_information")
    @Enumerated(EnumType.ORDINAL)
    private ContactInfo contactInfo;

    @Column(name = "views", columnDefinition = "bigint default 0")
    private long views;

    @Column(name = "is_deleted", columnDefinition = "boolean default false")
    private boolean isDeleted = false;

    @Column(name = "is_closed", columnDefinition = "boolean default false")
    private boolean isClosed = false;

}
