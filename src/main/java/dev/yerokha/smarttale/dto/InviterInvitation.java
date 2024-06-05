package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record InviterInvitation(
        @NotNull Long invitationId,
        @NotNull Long inviteeId,
        @NotNull String inviteeName,
        @NotNull String inviteeEmail,
        @NotNull Long positionId,
        @NotNull String position,
        @NotNull LocalDateTime invitedAt,
        @NotNull LocalDateTime expiresAt
) {
}
