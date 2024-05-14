package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// get market/id
public record FullOrderCard(
        @NotNull
        Long advertisementId,
        @NotNull
        String title,
        @NotNull
        String description,
        BigDecimal price,
        @NotNull
        List<String> imageUrls,
        String size,
        @NotNull
        LocalDateTime publishedAt,
        LocalDate deadlineAt,
        @NotNull
        Long acceptedBy,
        @NotNull
        String organizationName,
        @NotNull
        String organizationLogoUrl,
        @NotNull
        Long publishedBy,
        @NotNull
        String publisherName,
        @NotNull
        String publisherAvatarUrl,
        @NotNull
        String publisherPhoneNumber,
        @NotNull
        String publisherEmail,
        @NotNull
        long views
) implements AdvertisementInterface {
}
