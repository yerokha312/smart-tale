package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

// in get organization/positions
public record PositionSummary(
        @NotNull Long positionId,
        @NotNull String title
) {
}
