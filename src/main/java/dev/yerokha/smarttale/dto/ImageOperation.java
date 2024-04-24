package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ImageOperation(
        @Min(0) @Max(4)
        int arrayPosition,
        @Min(0) @Max(4)
        int targetPosition,
        @NotNull @NotEmpty
        Action action,
        @Min(0) @Max(4)
        int filePosition

) {
}
