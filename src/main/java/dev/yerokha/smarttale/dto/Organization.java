package dev.yerokha.smarttale.dto;

import java.time.LocalDate;

public record Organization(
        Long organizationId,
        Long ownerId,
        String ownerName,
        String ownerAvatarUrl,
        String organizationName,
        String description,
        LocalDate registeredAt,
        String logoUrl
) {
}
