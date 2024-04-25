package dev.yerokha.smarttale.dto;

import dev.yerokha.smarttale.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CurrentOrder(
        Long orderId,
        String title,
        String description,
        BigDecimal price,
        String imageUrl,
        OrderStatus orderStatus,
        LocalDate acceptedAt
) {
}
