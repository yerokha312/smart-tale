package dev.yerokha.smarttale.dto;

import dev.yerokha.smarttale.enums.ContextType;
import jakarta.validation.constraints.NotNull;

public record SearchItem(
        @NotNull Long id,
        @NotNull ContextType type,
        @NotNull String title,
        @NotNull String imageUrl
) {
}
