package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// in get market/id
public record ProductCard(
        @NotNull Long productId,
        @NotNull LocalDateTime publishedAt,
        @NotNull String title,
        @NotNull String description,
        @NotNull BigDecimal price,
        @NotNull int quantity,
        @NotNull List<String> imageUrls,
        @NotNull LocalDateTime purchasedAt,
        @NotNull Long publishedBy,
        @NotNull String publisherName,
        @NotNull String publisherAvatarUrl,
        @NotNull String publisherPhoneNumber,
        @NotNull String publisherEmail,
        @NotNull long views,
        @NotNull boolean canPurchase
) implements AdvertisementInterface {
}
