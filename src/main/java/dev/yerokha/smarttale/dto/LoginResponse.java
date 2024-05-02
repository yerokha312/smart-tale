package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

public record LoginResponse(
        @NotNull
        String accessToken,
        @NotNull
        String refreshToken,
        @NotNull
        Long userId
) {
}
