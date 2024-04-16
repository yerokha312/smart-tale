package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import static dev.yerokha.smarttale.service.AuthenticationService.CODE_LENGTH;

public record VerificationRequest(
        @NotNull @NotEmpty @Email
        String email,
        @Length(min = CODE_LENGTH, max = CODE_LENGTH, message = "Invalid code, please try again")
        String code
) {
}
