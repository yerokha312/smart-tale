package dev.yerokha.smarttale.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Product(
        Long productId,
        String title,
        String description,
        BigDecimal price,
        String imageUrl,
        LocalDateTime publishedAt
) implements AdvertisementInterface {
}
