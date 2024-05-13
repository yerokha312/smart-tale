package dev.yerokha.smarttale.dto;

import dev.yerokha.smarttale.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// in EmployeeTaskResponse
public record Task(
        @NotNull
        Long orderId,
        @NotNull
        OrderStatus status,
        @NotNull
        String title,
        @NotNull
        String key,
        @NotNull
        String description,
        BigDecimal price,
        String comment,
        @NotNull
        LocalDate date,
        @NotNull
        List<AssignedEmployee> employees,
        @NotNull
        Long publisherId,
        @NotNull
        String publisherName,
        String publisherAvatarUrl,
        @NotNull
        String publisherPhoneNumber

) {
}
