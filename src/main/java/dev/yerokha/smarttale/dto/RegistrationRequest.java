package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

public record RegistrationRequest(
        @NotNull @Length(min = 2, max = 30, message = "Name must be between 2 and 30 characters")
        @Pattern(regexp = "^[a-zA-Z\\s-]+$", message = "Name must contain only letters, hyphens and spaces")
        String firstName,
        @NotNull @Length(min = 2, max = 30, message = "Name must be between 2 and 30 characters")
        @Pattern(regexp = "^[a-zA-Z\\s-]+$", message = "Name must contain only letters, hyphens and spaces")
        String lastName,
        @Length(min = 2, max = 30, message = "Name must be between 2 and 30 characters")
        @Pattern(regexp = "^[a-zA-Z\\s-]+$", message = "Name must contain only letters, hyphens and spaces")
        String fatherName,
        @NotNull @Email @NotEmpty
        String email
) {
}
