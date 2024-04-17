package dev.yerokha.smarttale.entity.advertisement;

import dev.yerokha.smarttale.enums.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "orders")
public class OrderEntity extends Advertisement {

    @Column(name = "size")
    private String size;

    @Column(name = "deadline_at")
    private LocalDateTime deadlineAt;

    @Column(name = "status")
    private OrderStatus status;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

}