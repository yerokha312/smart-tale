package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record UpdateAdRequest(
        @NotNull @Positive
        Long advertisementId,
        @NotNull
        @Length(min = 5, max = 250, message = "Title length must be between 5 and 250")
        String title,
        @NotNull
        @Length(min = 5, max = 1000, message = "Description length must be between 5 and 1000")
        String description,
        @PositiveOrZero
        BigDecimal price,
        String size,
        @Future
        LocalDate deadlineAt,
        List<EditImage> editImages
) {
}
