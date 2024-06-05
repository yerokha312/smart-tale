package dev.yerokha.smarttale.dto;

import java.time.LocalDateTime;

public record JobApplication(
        Long applicationId,
        LocalDateTime applicationDate,
        Long applicantId,
        String applicantName,
        String avatarUrl,
        String email,
        String phoneNumber
) {
}
