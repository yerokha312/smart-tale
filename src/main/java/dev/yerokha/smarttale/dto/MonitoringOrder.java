package dev.yerokha.smarttale.dto;

import dev.yerokha.smarttale.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// get monitoring/id
public record MonitoringOrder(
        @NotNull
        Long orderId,
        @NotNull
        LocalDateTime publishedAt,
        LocalDate acceptedAt,
        LocalDate deadlineAt,
        String key,
        @NotNull
        String title,
        @NotNull
        String description,
        String size,
        List<String> imageUrls,
        @NotNull
        OrderStatus status,
        @NotNull
        Long publisherId,
        String publisherAvatarUrl,
        String publisherEmail,
        String publisherPhone,
        List<AssignedEmployee> employees,
        @NotNull
        long views
) {
}
