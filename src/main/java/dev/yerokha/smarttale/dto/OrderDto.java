package dev.yerokha.smarttale.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OrderDto(
        Long orderId,
        String title,
        String description,
        BigDecimal price,
        String size,
        LocalDate deadlineAt,
        List<String> imageUrls,
        Long publishedBy,
        String publisherAvatarUrl,
        String publisherName,
        String publisherPhoneNumber,
        String publisherEmail,
        LocalDate date
) {
}
