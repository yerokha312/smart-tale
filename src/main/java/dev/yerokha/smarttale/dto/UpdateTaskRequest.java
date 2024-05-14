package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;

import java.util.List;

public record UpdateTaskRequest(
        @NotNull @Positive
        Long taskId,
        @NotNull
        List<Long> addEmployees,
        @NotNull
        List<Long> removeEmployees,
        @Length(min = 1, max = 40)
        String comment
) {
}
