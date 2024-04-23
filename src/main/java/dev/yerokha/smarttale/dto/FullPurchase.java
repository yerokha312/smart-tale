package dev.yerokha.smarttale.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record FullPurchase(
        Long productId,
        String title,
        String description,
        BigDecimal price,
        List<String> imageUrl,
        LocalDateTime purchasedAt,
        Long publishedBy,
        String publisherName,
        String publisherAvatarUrl,
        String publisherPhoneNumber,
        String publisherEmail
) {
}
