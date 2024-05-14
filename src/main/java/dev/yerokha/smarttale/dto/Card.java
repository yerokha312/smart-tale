package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// used in paged purchases of user and marketplace
public record Card(
        @NotNull
        Long productId,
        @NotNull
        LocalDateTime publishedAt,
        @NotNull
        String title,
        @NotNull
        String description,
        BigDecimal price,
        @NotNull
        String imageUrl,
        @NotNull
        Long publishedBy,
        @NotNull
        String publisherName,
        @NotNull
        String publisherAvatarUrl
) {
}
