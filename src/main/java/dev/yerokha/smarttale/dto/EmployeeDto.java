package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

// used in EmployeeTaskResponse
public record EmployeeDto(
        @NotNull
        Long employeeId,
        @NotNull
        String name,
        @NotNull
        String avatarUrl,
        @NotNull
        String email,
        @NotNull
        String phoneNumber,
        @NotNull
        String position
) {
}
