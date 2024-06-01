package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record Invitation(
        @NotNull
        Long invitationId,
        @NotNull
        Long orgId,
        @NotNull
        String orgName,
        @NotNull
        String orgLogoUrl,
        @NotNull
        String position,
        @NotNull
        LocalDateTime invitedAt
) {
}
