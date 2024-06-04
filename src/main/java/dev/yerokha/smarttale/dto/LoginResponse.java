package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record LoginResponse(
        @NotNull String accessToken,
        @NotNull String refreshToken,
        @NotNull Long userId,
        @NotNull long organizationId,
        @NotNull int hierarchy,
        @NotNull List<String> authorities

) {
}
