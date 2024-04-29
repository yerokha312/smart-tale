package dev.yerokha.smarttale.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record FullProductCard(
        Long advertisementId,
        String title,
        String description,
        BigDecimal price,
        List<String> imageUrls,
        LocalDateTime publishedAt,
        LocalDateTime purchasedAt,
        Long publishedBy,
        String publisherName,
        String publisherAvatarUrl,
        String publisherPhoneNumber,
        String publisherEmail,
        long views
) implements AdvertisementInterface {
}
