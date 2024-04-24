package dev.yerokha.smarttale.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        Long userId
) {
}
