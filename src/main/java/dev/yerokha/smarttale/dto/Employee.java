package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

// used in paged list of employees
public record Employee(
        @NotNull Long employeeId,
        @NotNull String name,
        @NotNull String email,
        @NotNull List<OrderAccepted> orderList,
        @NotNull String position,
        @NotNull int hierarchy,
        @NotNull String status
) {
}
