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
        @NotNull
        BigDecimal price,
        @NotNull
        String imageUrl,
        @NotNull
        LocalDateTime publishedAt,
        @NotNull
        boolean isClosed
) implements AdvertisementInterface {
}
