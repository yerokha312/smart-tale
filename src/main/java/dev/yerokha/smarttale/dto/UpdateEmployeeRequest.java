package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateEmployeeRequest(
        @NotNull @Positive
        Long employeeId,
        @NotNull @Positive
        Long positionId
) {
}
