package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record JobApplication(
        @NotNull Long applicationId,
        @NotNull Long positionId,
        @NotNull LocalDateTime applicationDate,
        @NotNull Long applicantId,
        @NotNull String applicantName,
        @NotNull String avatarUrl,
        @NotNull String email,
        @NotNull String phoneNumber
) {
}
