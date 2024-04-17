package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

public record UpdateProfileRequest(
        @NotNull @Length(min = 2, max = 20, message = "Name must be between 2 and 20 characters")
        @Pattern(regexp = "^[\\p{IsLatin}&&[^\\p{IsCyrillic}]]+$|^[\\p{IsCyrillic}&&[^\\p{IsLatin}]]+$",
                message = "First name must be either Latin or Cyrillic, not a mix")
        String firstName,
        @NotNull @Length(min = 2, max = 20, message = "Name must be between 2 and 20 characters")
        @Pattern(regexp = "^[\\p{IsLatin}&&[^\\p{IsCyrillic}]]+$|^[\\p{IsCyrillic}&&[^\\p{IsLatin}]]+$",
                message = "Last name must be either Latin or Cyrillic, not a mix")
        String lastName,
        @NotNull @Length(max = 20, message = "Name must be between 2 and 20 characters")
        @Pattern(regexp = "^[\\p{IsLatin}&&[^\\p{IsCyrillic}]]+$|^[\\p{IsCyrillic}&&[^\\p{IsLatin}]]+$",
                message = "Father name must be either Latin or Cyrillic, not a mix")
        String fatherName,
        @NotNull @Email @NotEmpty
        String email,
        String phoneNumber
) {
    public boolean isValid() {
        boolean isFirstLatin = firstName.matches("^[a-zA-Z\\s-]+$");
        boolean isLastLatin = lastName.matches("^[a-zA-Z\\s-]+$");
        boolean isFatherLatin = fatherName == null || fatherName.matches("^[a-zA-Z\\s-]*$");

        // All fields are consistent (either all Latin or all Cyrillic)
        return isFirstLatin == isLastLatin && (fatherName == null || isFatherLatin == isFirstLatin);
    }
}
