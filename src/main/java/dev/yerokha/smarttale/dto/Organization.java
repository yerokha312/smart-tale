package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

// in get organizations/recipientId
public record Organization(
        @NotNull
        Long organizationId,
        @NotNull
        String name,
        @NotNull
        String description,
        @NotNull
        String logoUrl,
        @NotNull
        Long ownerId,
        @NotNull
        String ownerName,
        @NotNull
        String ownerAvatarUrl,
        @NotNull
        LocalDate registeredAt
) {
}
