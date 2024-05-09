package dev.yerokha.smarttale.dto;

import org.springframework.data.domain.Page;

// used in get employees/id
public record EmployeeTasksResponse(
        EmployeeDto employee,
        Page<Task> tasks
) {
}
