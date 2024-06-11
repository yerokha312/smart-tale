package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

public record UserSummary(
        @NotNull Long userId,
        @NotNull String name,
        @NotNull String avatarUrl,
        @NotNull Long organizationId,
        @NotNull String organizationName,
        @NotNull String organizationLogoUrl,
        @NotNull boolean isSubscribed
) {
}
