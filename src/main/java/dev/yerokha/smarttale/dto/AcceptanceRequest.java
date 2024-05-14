package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

// for inner usage
public record AcceptanceRequest(
        @NotNull
        String title,
        @NotNull
        String description,
        String price,
        @NotNull
        String organizationUrl,
        @NotNull
        String organizationName,
        @NotNull
        String organizationLogo
) {
}
