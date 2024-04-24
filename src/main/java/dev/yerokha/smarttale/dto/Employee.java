package dev.yerokha.smarttale.dto;

import java.util.List;

public record Employee(
        Long employeeId,
        String name,
        String email,
        List<CurrentOrder> orderList,
        String position,
        String status
) {
}
