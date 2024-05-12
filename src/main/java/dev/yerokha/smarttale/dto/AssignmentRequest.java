package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record AssignmentRequest(
        @NotNull @Positive
        Long taskId,
        @NotNull @NotEmpty
        List<Long> employeeIds
) {
}
