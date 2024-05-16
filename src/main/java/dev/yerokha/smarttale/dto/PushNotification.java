package dev.yerokha.smarttale.dto;

import java.util.Map;

public record PushNotification(
        Long id,
        Map<String, String> data
) {
}
