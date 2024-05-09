package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

public record Position(
        Long positionId,
        @NotNull
        String title,
        String description,
        @NotNull
        int authorities,
        @NotNull
        Long organizationId
) {
}
