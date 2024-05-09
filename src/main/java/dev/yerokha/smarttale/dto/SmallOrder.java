package dev.yerokha.smarttale.dto;

import dev.yerokha.smarttale.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

// in get my orders
public record SmallOrder(
        @NotNull
        Long orderId,
        @NotNull
        String title,
        BigDecimal price,
        @NotNull
        LocalDate acceptedAt,
        LocalDate deadlineAt,
        LocalDate completedAt,
        OrderStatus status
) {
}
