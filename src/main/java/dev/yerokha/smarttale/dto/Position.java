package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;

import java.util.List;

public record Position(
        Long positionId,
        @NotNull @Length(min = 2)
        String title,
        @NotNull @PositiveOrZero
        Integer hierarchy,
        @NotNull @Size(min = 1)
        List<String> authorities,
        @NotNull
        Long organizationId
) {
}
