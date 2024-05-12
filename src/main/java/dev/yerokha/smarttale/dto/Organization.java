package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

// in get organizations/id
public record Organization(
        @NotNull
        Long organizationId,
        @NotNull
        String name,
        String description,
        String logoUrl,
        @NotNull
        Long ownerId,
        @NotNull
        String ownerName,
        String ownerAvatarUrl,
        @NotNull
        LocalDate registeredAt
) {
}
