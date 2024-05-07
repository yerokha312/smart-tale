package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record FullProduct(
        @NotNull
        Long productId,
        @NotNull
        LocalDateTime publishedAt,
        @NotNull
        String title,
        @NotNull
        String description,
        BigDecimal price,
        List<String> imageUrls,
        @NotNull
        long views,
        @NotNull
        boolean isDeleted,
        @NotNull
        boolean isClosed
) implements AdvertisementInterface {
}
