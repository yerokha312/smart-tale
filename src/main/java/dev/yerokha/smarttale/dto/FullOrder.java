package dev.yerokha.smarttale.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record FullOrder(
        Long orderId,
        LocalDateTime publishedAt,
        Long publishedBy,
        LocalDateTime acceptedAt,
        Long acceptedBy,
        String title,
        String description,
        BigDecimal price,
        List<String> imageUrls,
        long views,
        boolean isDeleted,
        boolean isClosed
) implements AdvertisementInterface {
}
