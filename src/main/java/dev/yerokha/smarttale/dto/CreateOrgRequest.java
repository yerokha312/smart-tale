package dev.yerokha.smarttale.dto;

import jakarta.validation.constraints.NotNull;


public record CreateOrgRequest(
        @NotNull String name,
        String description

) {
}
