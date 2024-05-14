package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// in personal account get my advertisements
public record Order(
        @NotNull
        Long orderId,
        @NotNull
        String title,
        @NotNull
        String description,
        BigDecimal price,
        @NotNull
        String imageUrl,
        @NotNull
        LocalDateTime publishedAt
) implements AdvertisementInterface {
}
