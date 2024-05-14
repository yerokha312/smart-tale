package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// used in get my advertisements/id
public record FullOrder(
        @NotNull
        Long orderId,
        @NotNull
        LocalDateTime publishedAt,
        LocalDate acceptedAt,
        @NotNull
        Long acceptedBy,
        @NotNull
        List<AcceptanceRequestDto> acceptanceRequests,
        @NotNull
        String organizationName,
        @NotNull
        String organizationLogoUrl,
        @NotNull
        String title,
        @NotNull
        String description,
        BigDecimal price,
        String size,
        LocalDate deadlineAt,
        @NotNull
        List<String> imageUrls,
        @NotNull
        long views,
        @NotNull
        boolean isDeleted,
        @NotNull
        boolean isClosed
) implements AdvertisementInterface {
}
