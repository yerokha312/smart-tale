package dev.yerokha.smarttale.dto;

import dev.yerokha.smarttale.enums.JobType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record JobCard(
        @NotNull Long jobId,
        @NotNull LocalDateTime publishedAt,
        @NotNull String title,
        @NotNull String description,
        @NotNull BigDecimal salary,
        @NotNull List<String> imageUrls,
        @NotNull Long publishedBy,
        @NotNull String publisherName,
        @NotNull String publisherAvatarUrl,
        @NotNull String publisherPhoneNumber,
        @NotNull String publisherEmail,
        @NotNull Long organizationId,
        @NotNull String organizationName,
        @NotNull String organizationLogoUrl,
        @NotNull JobType jobType,
        @NotNull int applicantsCount,
        @NotNull String location,
        LocalDate applicationDeadline,
        @NotNull long views,
        @NotNull boolean canApply
) implements AdvertisementInterface {
}
