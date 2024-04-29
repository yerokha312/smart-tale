package dev.yerokha.smarttale.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record FullOrderCard(
        Long advertisementId,
        String title,
        String description,
        BigDecimal price,
        List<String> imageUrls,
        String size,
        LocalDateTime publishedAt,
        LocalDate deadlineAt,
        Long acceptedBy,
        String acceptorName,
        String acceptorAvatarUrl,
        Long publishedBy,
        String publisherName,
        String publisherAvatarUrl,
        String publisherPhoneNumber,
        String publisherEmail,
        long views
) implements AdvertisementInterface {
}
