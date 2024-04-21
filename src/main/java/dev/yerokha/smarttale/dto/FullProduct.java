package dev.yerokha.smarttale.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record FullProduct(
        Long productId,
        LocalDateTime publishedAt,
        Long publishedBy,
        LocalDateTime purchasedAt,
        Long purchasedBy,
        String title,
        String description,
        BigDecimal price,
        List<String> imageUrls,
        long views,
        boolean isDeleted,
        boolean isClosed
) implements AdvertisementInterface {
}
