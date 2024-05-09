package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// in get my advertisements
public record Product(
        @NotNull
        Long productId,
        @NotNull
        String title,
        @NotNull
        String description,
        BigDecimal price,
        String imageUrl,
        @NotNull
        LocalDateTime publishedAt
) implements AdvertisementInterface {
}
