package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

public record AssignedEmployee(
        @NotNull
        Long userId,
        @NotNull
        String name,
        String avatarUrl
) {
}
