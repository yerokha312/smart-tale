package dev.yerokha.smarttale.dto;

import dev.yerokha.smarttale.enums.PersonalAdvertisementType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// common DTO for Order and Product for retrieving both from repo
public record AdvertisementDto(
        @NotNull PersonalAdvertisementType type,
        @NotNull Long advertisementId,
        @NotNull String title,
        @NotNull String description,
        @NotNull BigDecimal price,
        @NotNull int quantity,
        @NotNull String imageUrl,
        @NotNull LocalDateTime publishedAt,
        @NotNull int acceptancesCount,
        @NotNull boolean isClosed
) implements AdvertisementInterface {
}
