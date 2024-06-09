package dev.yerokha.smarttale.dto;

import dev.yerokha.smarttale.enums.JobType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record Job(
        @NotNull Long jobId,
        @NotNull LocalDateTime publishedAt,
        @NotNull Long publishedBy,
        @NotNull String publisherName,
        @NotNull String publisherAvatarUrl,
        @NotNull String title,
        @NotNull Long positionId,
        @NotNull String position,
        @NotNull JobType jobType,
        @NotNull String location,
        @NotNull BigDecimal salary,
        @NotNull String description,
        @NotNull List<String> images,
        @NotNull List<JobApplication> jobApplications,
        LocalDate applicationDeadline,
        @NotNull long views,
        @NotNull boolean isDeleted,
        @NotNull boolean isClosed,
        @NotNull boolean canModify
) {
}
