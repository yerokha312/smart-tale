package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

// used in FullOrder to show who wants to accept the order
public record AcceptanceRequest(
        @NotNull Long organizationId,
        @NotNull String name,
        @NotNull String logoUrl,
        @NotNull String code,
        @NotNull LocalDate requestedAt
) {
}
