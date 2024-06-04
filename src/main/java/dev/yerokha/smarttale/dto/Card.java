package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Card(
        @NotNull Long advertisementId,
        @NotNull LocalDateTime publishedAt,
        @NotNull String title,
        @NotNull String description,
        @NotNull BigDecimal price,
        @NotNull String imageUrl,
        @NotNull Long publishedBy,
        @NotNull String publisherName,
        @NotNull String publisherAvatarUrl,
        @NotNull boolean canHandle
) {
}
