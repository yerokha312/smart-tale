package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record InviteRequest(
        @NotNull @NotEmpty @Email
        String email,
        @NotNull @Positive
        Long positionId
) {
}
