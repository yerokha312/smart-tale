package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

// used in FullOrder to show who wants to accept the order
public record AcceptanceRequestDto(
        @NotNull Long organizationId,
        @NotNull String name,
        @NotNull String logoUrl,
        @NotNull String code
) {
}
