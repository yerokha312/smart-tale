package dev.yerokha.smarttale.dto;

import dev.yerokha.smarttale.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

// in get organizations/orders
public record OrderAccepted(
        @NotNull Long orderId,
        @NotNull String key,
        @NotNull String title,
        @NotNull String description,
        @NotNull BigDecimal price,
        @NotNull String imageUrl,
        @NotNull OrderStatus status,
        @NotNull LocalDate acceptedAt,
        LocalDate deadlineAt,
        LocalDate completedAt
) {
}
