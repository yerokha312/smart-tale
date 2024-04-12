package dev.yerokha.smarttale.entity.advertisement;

import dev.yerokha.smarttale.entity.Image;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@MappedSuperclass
public abstract class Advertisement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "advertisement_id")
    private Long advertisementId;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserDetailsEntity postedUser;

    @Column(name = "title", nullable = false, length = 250)
    private String title;

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @Column(name = "price")
    private BigDecimal price;

    @ManyToMany
    @JoinTable(
            name = "advertisement_image_junction",
            joinColumns = @JoinColumn(name = "advertisement_id"),
//            uniqueConstraints = @UniqueConstraint(columnNames = {"advertisement_id", "image_id"}),
            inverseJoinColumns = @JoinColumn(name = "image_id")
    )
    @Size(max = 5)
    private List<Image> images;

    @Column(name = "views")
    private Long views;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @Column(name = "is_hidden")
    private boolean isHidden;
}
