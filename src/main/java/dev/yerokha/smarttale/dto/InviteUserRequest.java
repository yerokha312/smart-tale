package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record InviteUserRequest(
        @NotNull @Positive Long inviteeId,
        @NotNull @Positive Long positionId
) {
}
