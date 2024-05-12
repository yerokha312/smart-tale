package dev.yerokha.smarttale.dto;

import java.util.List;

public record PositionDto(
        Long positionId,
        String title,
        int hierarchy,
        List<String> authorities
) {
}
