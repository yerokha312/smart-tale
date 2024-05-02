package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OrderDto(
        @NotNull
        Long orderId,
        @NotNull
        String title,
        @NotNull
        String description,
        BigDecimal price,
        String size,
        LocalDate deadlineAt,
        List<String> imageUrls,
        @NotNull
        Long publishedBy,
        @NotNull
        String publisherAvatarUrl,
        @NotNull
        String publisherName,
        String publisherPhoneNumber,
        @NotNull
        String publisherEmail,
        @NotNull
        LocalDate date
) {
}
