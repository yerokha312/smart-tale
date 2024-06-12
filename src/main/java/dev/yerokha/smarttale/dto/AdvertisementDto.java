package dev.yerokha.smarttale.dto;

import dev.yerokha.smarttale.enums.PersonalAdvertisementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// common DTO for Order and Product for retrieving both from repo
public record AdvertisementDto(
        PersonalAdvertisementType type,
        Long advertisementId,
        String title,
        String description,
        BigDecimal price,
        int quantity,
        String imageUrl,
        LocalDateTime publishedAt,
        int acceptancesCount,
        boolean isClosed
) implements AdvertisementInterface {
}
