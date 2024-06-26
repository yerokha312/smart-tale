package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;

// used in get employees/id
public record EmployeeTasksResponse(
        @NotNull EmployeeDto employee,
        @NotNull Page<Task> tasks
) {
}
