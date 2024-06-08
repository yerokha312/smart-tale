package dev.yerokha.smarttale.dto;

import dev.yerokha.smarttale.enums.PurchaseStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PurchaseSummary(
        @NotNull Long purchaseId,
        @NotNull LocalDateTime purchasedAt,
        @NotNull PurchaseStatus status,
        @NotNull Long productId,
        @NotNull String title,
        @NotNull String description,
        @NotNull BigDecimal totalPrice,
        @NotNull String imageUrl,
        @NotNull Long publishedBy,
        @NotNull String publisherName,
        @NotNull String publisherAvatarUrl,
        @NotNull boolean canRepeatPurchase
) {
}
