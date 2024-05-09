package dev.yerokha.smarttale.dto;

import dev.yerokha.smarttale.enums.ContactInfo;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.time.LocalDate;

// used in post market
public record CreateAdRequest(
        @NotNull @NotEmpty
        String type,
        @NotNull @NotEmpty
        @Length(min = 5, max = 250, message = "Title length must be between 5 and 250")
        String title,
        @NotNull @NotEmpty
        @Length(min = 5, max = 1000, message = "Description length must be between 5 and 1000")
        String description,
        @PositiveOrZero
        BigDecimal price,
        String size,
        @Future
        LocalDate deadline,
        ContactInfo contactInfo
) {
}
