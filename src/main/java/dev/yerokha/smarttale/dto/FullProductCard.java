package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record FullProductCard(
        @NotNull
        Long advertisementId,
        @NotNull
        String title,
        @NotNull
        String description,
        @NotNull
        BigDecimal price,
        List<String> imageUrls,
        @NotNull
        LocalDateTime publishedAt,
        LocalDateTime purchasedAt,
        @NotNull
        Long publishedBy,
        @NotNull
        String publisherName,
        String publisherAvatarUrl,
        @NotNull
        String publisherPhoneNumber,
        @NotNull
        String publisherEmail,
        @NotNull
        long views
) implements AdvertisementInterface {
}
