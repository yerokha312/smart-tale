package dev.yerokha.smarttale.dto;

import dev.yerokha.smarttale.entity.PushNotificationEntity;

import java.util.List;

public record NotificationSliceContainer(
        List<PushNotificationEntity> content,
        boolean hasNext
) {
}
