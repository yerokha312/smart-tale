package dev.yerokha.smarttale.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Purchase(
        Long productId,
        String title,
        String description,
        BigDecimal price,
        String imageUrl,
        LocalDateTime purchasedAt,
        Long publishedBy,
        String publisherAvatarUrl
) {
}
