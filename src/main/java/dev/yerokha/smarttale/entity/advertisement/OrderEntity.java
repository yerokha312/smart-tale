package dev.yerokha.smarttale.entity.advertisement;

import dev.yerokha.smarttale.entity.user.OrganizationEntity;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.enums.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "orders")
public class OrderEntity extends Advertisement {

    @Column(name = "size")
    private String size;

    @Column(name = "deadline_at")
    private LocalDate deadlineAt; //sort

    @Column(name = "status")
    private OrderStatus status; //sort

    @ManyToOne
    @JoinColumn(name = "accepted_by")
    private OrganizationEntity acceptedBy;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<AcceptanceEntity> acceptanceEntities;

    @Column(name = "accepted_at")
    private LocalDate acceptedAt; //sort

    @Column(name = "task_key")
    private String taskKey;

    @Column(name = "comment")
    private String comment;

    @ManyToMany
    @JoinTable(
            name = "task_employee_junction",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<UserDetailsEntity> contractors;

    @Column(name = "completed_at")
    private LocalDate completedAt;

    public void addAcceptanceRequest(AcceptanceEntity acceptance) {
        if (this.acceptanceEntities == null) {
            this.acceptanceEntities = new ArrayList<>();
        }

        this.acceptanceEntities.add(acceptance);
    }

}
