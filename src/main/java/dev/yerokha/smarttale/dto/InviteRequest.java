package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record InviteRequest(
        @NotNull @NotEmpty @Email
        String email,
        @NotNull @NotEmpty
        Long positionId
) {
}
