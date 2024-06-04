package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// get market/id
public record FullOrderCard(
        @NotNull Long orderId,
        @NotNull LocalDateTime publishedAt,
        @NotNull String title,
        @NotNull String description,
        @NotNull BigDecimal price,
        @NotNull List<String> imageUrls,
        @NotNull String size,
        LocalDate deadlineAt,
        @NotNull Long publishedBy,
        @NotNull String publisherName,
        @NotNull String publisherAvatarUrl,
        @NotNull String publisherPhoneNumber,
        @NotNull String publisherEmail,
        @NotNull long views,
        @NotNull boolean canAccept
) implements AdvertisementInterface {
}
