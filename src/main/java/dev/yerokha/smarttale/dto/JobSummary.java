package dev.yerokha.smarttale.dto;

import dev.yerokha.smarttale.enums.JobType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record JobSummary(
        Long jobId,
        LocalDateTime publishedAt,
        String title,
        String description,
        JobType jobType,
        BigDecimal salary,
        String imageUrl,
        int applicantsCount,
        boolean isClosed
) {
}
