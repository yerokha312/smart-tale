package dev.yerokha.smarttale.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SmallOrder(
        Long orderId,
        String title,
        BigDecimal price,
        LocalDate date
) {
}
