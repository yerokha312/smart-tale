package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record Employee(
        @NotNull
        Long employeeId,
        @NotNull
        String name,
        @NotNull
        String email,
        List<CurrentOrder> orderList,
        @NotNull
        String position,
        @NotNull
        String status
) {
}
