package dev.yerokha.smarttale.dto;

import dev.yerokha.smarttale.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CurrentOrder(
        @NotNull
        Long orderId,
        @NotNull
        String title,
        @NotNull
        String description,
        BigDecimal price,
        String imageUrl,
        @NotNull
        OrderStatus status,
        @NotNull
        LocalDate acceptedAt,
        LocalDate deadlineAt
) {
}
