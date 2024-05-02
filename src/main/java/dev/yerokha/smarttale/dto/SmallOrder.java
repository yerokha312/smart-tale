package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SmallOrder(
        @NotNull
        Long orderId,
        @NotNull
        String title,
        BigDecimal price,
        @NotNull
        LocalDate date
) {
}
