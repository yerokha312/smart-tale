package dev.yerokha.smarttale.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Card(
        Long productId,
        LocalDateTime publishedAt,
        String title,
        String description,
        BigDecimal price,
        String imageUrl,
        Long publishedBy,
        String publisherAvatarUrl,
        LocalDateTime date

) {
}
