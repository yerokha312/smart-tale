package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PositionDto(
        @NotNull Long positionId,
        @NotNull String title,
        @NotNull int hierarchy,
        @NotNull List<String> authorities
) {
}
