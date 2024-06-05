package dev.yerokha.smarttale.dto;

import dev.yerokha.smarttale.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

// used in list from monitoring dashboard
public record OrderDashboard(
        @NotNull Long id,
        @NotNull OrderStatus status,
        @NotNull String title,
        @NotNull String key,
        @NotNull String comment,
        LocalDate deadlineAt

) {
}
