package dev.yerokha.smarttale.dto;

// used in EmployeeTaskResponse
public record EmployeeDto(
        Long employeeId,
        String name,
        String avatarUrl,
        String email,
        String phoneNumber,
        String position
) {
}
