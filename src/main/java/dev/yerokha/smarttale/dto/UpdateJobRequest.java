package dev.yerokha.smarttale.dto;

import dev.yerokha.smarttale.enums.ContactInfo;
import dev.yerokha.smarttale.enums.JobType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record UpdateJobRequest(
        @NotNull @Positive
        Long jobId,
        @NotNull @Length(min = 5, max = 250, message = "Title length must be between 5 and 250")
        String title,
        @NotNull @Length(min = 5, max = 1000, message = "Description length must be between 5 and 1000")
        String description,
        List<ImageOperation> imageOperations,
        @NotNull
        ContactInfo contactInfo,
        @NotNull @Positive
        Long positionId,
        @NotNull
        JobType jobType,
        @NotNull @NotEmpty
        String location,
        @PositiveOrZero
        BigDecimal salary,
        @FutureOrPresent
        LocalDate applicationDeadline
) implements UpdateAdInterface {
}
