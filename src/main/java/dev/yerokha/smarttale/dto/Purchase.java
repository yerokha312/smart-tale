package dev.yerokha.smarttale.dto;

import dev.yerokha.smarttale.enums.PurchaseStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Purchase(
        @NotNull Long purchaseId,
        @NotNull LocalDateTime purchasedAt,
        @NotNull PurchaseStatus status,
        @NotNull LocalDateTime statusDate,
        @NotNull Long productId,
        @NotNull String title,
        @NotNull String description,
        @NotNull int quantity,
        @NotNull BigDecimal price,
        @NotNull BigDecimal totalPrice,
        @NotNull String imageUrl,
        @NotNull Long publishedBy,
        @NotNull String publisherName,
        @NotNull String publisherAvatarUrl,
        @NotNull String publisherPhoneNumber,
        @NotNull String publisherEmail,
        @NotNull boolean canRepeatPurchase
) {
}
