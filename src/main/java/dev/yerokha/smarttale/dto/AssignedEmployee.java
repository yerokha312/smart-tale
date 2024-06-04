package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

// used in orders of organization, including dashboard order
public record AssignedEmployee(
        @NotNull Long userId,
        @NotNull String name,
        @NotNull String avatarUrl,
        @NotNull BigDecimal reward
) {
}
