package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;

public record NotificationHistoryRequest(
        Long userId,
        Long organizationId,
        @PositiveOrZero
        int page,
        @Min(5)
        int size
) {
}
