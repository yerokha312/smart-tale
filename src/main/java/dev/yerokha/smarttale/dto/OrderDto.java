package dev.yerokha.smarttale.dto;

import dev.yerokha.smarttale.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// account get my orders/recipientId
public record OrderDto(
        @NotNull
        Long orderId,
        @NotNull
        OrderStatus status,
        @NotNull
        String title,
        @NotNull
        String description,
        @NotNull
        BigDecimal price,
        @NotNull
        String size,
        @NotNull
        Long acceptedBy,
        @NotNull
        String organizationName,
        @NotNull
        String organizationLogoUrl,
        @NotNull
        LocalDate acceptedAt,
        LocalDate deadlineAt,
        LocalDate completedAt,
        @NotNull
        List<String> imageUrls

) {
}
