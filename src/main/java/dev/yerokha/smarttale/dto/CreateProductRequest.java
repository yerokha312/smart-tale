package dev.yerokha.smarttale.dto;

import dev.yerokha.smarttale.enums.ContactInfo;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

public record CreateProductRequest(
        @NotNull @NotEmpty
        @Length(min = 5, max = 250, message = "Title length must be between 5 and 250")
        String title,
        @NotNull @NotEmpty
        @Length(min = 5, max = 1000, message = "Description length must be between 5 and 1000")
        String description,
        @Positive @NotNull
        int quantity,
        @PositiveOrZero @NotNull
        BigDecimal price,
        @NotNull
        ContactInfo contactInfo
) implements CreateAdInterface {
}
