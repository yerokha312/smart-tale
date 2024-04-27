package dev.yerokha.smarttale.entity.advertisement;

import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.enums.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

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
    private UserDetailsEntity acceptedBy;

    @Column(name = "accepted_at")
    private LocalDate acceptedAt; //sort

    @Column(name = "completed_at")
    private LocalDate completedAt;

}
