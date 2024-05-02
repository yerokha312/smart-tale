package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

public record Position(
        @NotNull
        Long positionId,
        @NotNull
        String title
) {
}
