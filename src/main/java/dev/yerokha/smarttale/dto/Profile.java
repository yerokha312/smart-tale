package dev.yerokha.smarttale.dto;

import java.time.LocalDate;

public record Profile(
        String firstName,
        String lastName,
        String middleName,
        String email,
        String phoneNumber,
        String avatarUrl,
        LocalDate subscriptionEndDate
) {
}
