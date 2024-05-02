package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record Profile(
        @NotNull
        String firstName,
        @NotNull
        String lastName,
        @NotNull
        String middleName,
        @NotNull
        String email,
        String phoneNumber,
        String avatarUrl,
        LocalDate subscriptionEndDate
) {
}
