package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record Organization(
        @NotNull
        Long organizationId,
        @NotNull
        Long ownerId,
        @NotNull
        String ownerName,
        String ownerAvatarUrl,
        @NotNull
        String organizationName,
        String description,
        @NotNull
        LocalDate registeredAt,
        String logoUrl
) {
}
