package dev.yerokha.smarttale.dto;

import java.util.Map;

public record PushNotification(
        Long recipientId,
        Map<String, String> data
) {
}
