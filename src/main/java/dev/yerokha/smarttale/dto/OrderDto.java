package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OrderDto(
        @NotNull
        Long orderId,
        @NotNull
        String title,
        @NotNull
        String description,
        BigDecimal price,
        String size,
        @NotNull
        Long acceptedBy,
        @NotNull
        String acceptorName,
        String acceptorLogoUrl,
        @NotNull
        LocalDate acceptedAt,
        LocalDate deadlineAt,
        LocalDate completedAt,
        List<String> imageUrls

) {
}
