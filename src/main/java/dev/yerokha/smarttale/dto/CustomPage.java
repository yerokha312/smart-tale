package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CustomPage<T>(
        @NotNull List<T> content,
        @NotNull int totalPages,
        @NotNull long totalElements,
        @NotNull int number,
        @NotNull int size,
        @NotNull boolean isEmpty
) {
}
