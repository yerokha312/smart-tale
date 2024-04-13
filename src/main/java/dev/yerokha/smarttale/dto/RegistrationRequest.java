package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

public record RegistrationRequest(
        @NotNull @Length(min = 2, max = 20, message = "Name must be between 2 and 20 characters")
        @Pattern.List({
                @Pattern(regexp = "^[a-zA-Z\\s-]+$", message = "Name must contain either Latin or Cyrillic characters, hyphens, and spaces"),
                @Pattern(regexp = "^[\\p{IsCyrillic}\\s-]+$", message = "Name must contain either Latin or Cyrillic characters, hyphens, and spaces")
        })
        String firstName,
        @NotNull @Length(min = 2, max = 20, message = "Name must be between 2 and 20 characters")
        @Pattern.List({
                @Pattern(regexp = "^[a-zA-Z\\s-]+$", message = "Name must contain either Latin or Cyrillic characters, hyphens, and spaces"),
                @Pattern(regexp = "^[\\p{IsCyrillic}\\s-]+$", message = "Name must contain either Latin or Cyrillic characters, hyphens, and spaces")
        })
        String lastName,
        @Length(min = 2, max = 20, message = "Name must be between 2 and 20 characters")
        @Pattern.List({
                @Pattern(regexp = "^[a-zA-Z\\s-]+$", message = "Name must contain either Latin or Cyrillic characters, hyphens, and spaces"),
                @Pattern(regexp = "^[\\p{IsCyrillic}\\s-]+$", message = "Name must contain either Latin or Cyrillic characters, hyphens, and spaces")
        })
        String fatherName,
        @NotNull @Email @NotEmpty
        String email
) {
        public boolean isValid() {
                boolean isFirstLatin = firstName.matches("^[a-zA-Z\\s-]+$");
                boolean isLastLatin = lastName.matches("^[a-zA-Z\\s-]+$");
                boolean isFatherLatin = fatherName == null || fatherName.matches("^[a-zA-Z\\s-]*$");

            // All fields are consistent (either all Latin or all Cyrillic)
            return isFirstLatin == isLastLatin && (fatherName == null || isFatherLatin == isFirstLatin);
        }
}
