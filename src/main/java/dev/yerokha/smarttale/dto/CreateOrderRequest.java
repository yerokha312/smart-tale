package dev.yerokha.smarttale.dto;

import dev.yerokha.smarttale.enums.ContactInfo;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateOrderRequest(
        @NotNull @NotEmpty
        @Length(min = 5, max = 250, message = "Title length must be between 5 and 250")
        String title,
        @NotNull @NotEmpty
        @Length(min = 5, max = 1000, message = "Description length must be between 5 and 1000")
        String description,
        @PositiveOrZero
        BigDecimal price,
        String size,
        @FutureOrPresent
        LocalDate deadline,
        @NotNull
        ContactInfo contactInfo
) implements CreateAdInterface {
}
