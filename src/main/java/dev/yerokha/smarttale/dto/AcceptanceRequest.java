package dev.yerokha.smarttale.dto;

public record AcceptanceRequest(
        String title,
        String description,
        String price,
        String organizationUrl,
        String organizationName,
        String organizationLogo
) {
}
