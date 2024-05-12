package dev.yerokha.smarttale.dto;

import dev.yerokha.smarttale.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// in EmployeeTaskResponse
public record Task(
        Long orderId,
        OrderStatus status,
        String title,
        String key,
        String description,
        BigDecimal price,
        String comment,
        LocalDate date,
        List<AssignedEmployee> employees,
        Long publisherId,
        String publisherName,
        String publisherAvatarUrl,
        String publisherPhoneNumber

) {
}
