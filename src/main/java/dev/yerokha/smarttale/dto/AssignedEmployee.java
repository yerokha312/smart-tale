package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

// used in orders of organization, including dashboard order
public record AssignedEmployee(
        @NotNull
        Long userId,
        @NotNull
        String name,
        String avatarUrl,
        BigDecimal reward
) {
}
