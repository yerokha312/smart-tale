package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

// in get organizations
public record OrganizationSummary(
        @NotNull Long organizationId,
        @NotNull String name,
        @NotNull String logoUrl
) {
}
