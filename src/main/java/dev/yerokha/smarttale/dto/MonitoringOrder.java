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
        @NotNull
        String key,
        @NotNull
        String title,
        @NotNull
        String description,
        @NotNull
        String size,
        @NotNull
        List<String> imageUrls,
        @NotNull
        OrderStatus status,
        @NotNull
        Long publisherId,
        @NotNull
        String publisherAvatarUrl,
        @NotNull
        String publisherEmail,
        @NotNull
        String publisherPhone,
        @NotNull
        List<AssignedEmployee> employees,
        @NotNull
        long views
) {
}
