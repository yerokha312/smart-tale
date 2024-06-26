package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// in post /organizations/employees
public record InviteRequest(
        String lastName,
        String firstName,
        String middleName,
        @NotNull @NotEmpty @Email
        String email,
        @NotNull @NotEmpty
        String phoneNumber,
        @NotNull @Positive
        Long positionId
) {
    public String getName() {
        if (lastName != null) {
            if (middleName != null) {
                return lastName + " " + firstName + " " + middleName;
            }
            return lastName + " " + firstName;

        }
        return null;
    }
}
