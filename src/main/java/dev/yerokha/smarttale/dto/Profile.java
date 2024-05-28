package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

// in get account/profile
public record Profile(
        @NotNull
        String firstName,
        @NotNull
        String lastName,
        @NotNull
        String middleName,
        @NotNull
        String email,
        @NotNull
        String phoneNumber,
        @NotNull
        String avatarUrl,
        @NotNull
        Long organizationId,
        @NotNull
        String organizationName,
        @NotNull
        String organizationLogoUrl,
        LocalDate subscriptionEndDate
) {
}
