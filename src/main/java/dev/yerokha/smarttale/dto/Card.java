package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
        String imageUrl,
        @NotNull
        Long publishedBy,
        String publisherAvatarUrl,
        @NotNull
        LocalDateTime date

) {
}
