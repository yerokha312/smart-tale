package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UserDto(
        @NotNull Long userId,
        @NotNull String name,
        @NotNull String avatarUrl,
        @NotNull Long organizationId,
        @NotNull String organizationName,
        @NotNull String organizationLogoUrl,
        @NotNull String position,
        @NotNull String email,
        @NotNull String phoneNumber,
        @NotNull LocalDate registeredAt,
        @NotNull boolean isSubscribed,
        @NotNull boolean canInvite
) {
}
