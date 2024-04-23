package dev.yerokha.smarttale.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record FullOrder(
        Long orderId,
        LocalDateTime publishedAt,
        Long publishedBy,
        LocalDate acceptedAt,
        Long acceptedBy,
        String title,
        String description,
        BigDecimal price,
        String size,
        LocalDate deadlineAt,
        List<String> imageUrls,
        long views,
        boolean isDeleted,
        boolean isClosed
) implements AdvertisementInterface {
}