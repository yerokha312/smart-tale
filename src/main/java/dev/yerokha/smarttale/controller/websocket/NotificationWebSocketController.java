package dev.yerokha.smarttale.controller.websocket;

import dev.yerokha.smarttale.dto.NotificationHistoryRequest;
import dev.yerokha.smarttale.service.PushNotificationService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationWebSocketController {

    private final PushNotificationService pushNotificationService;

    public NotificationWebSocketController(PushNotificationService pushNotificationService) {
        this.pushNotificationService = pushNotificationService;
    }

    @MessageMapping("/notifications/markAsRead")
    public void markAsRead(@Payload Long notificationId) {
        pushNotificationService.markNotificationAsRead(notificationId);
    }

    @MessageMapping("/notifications/history")
    public void getHistory(@Payload NotificationHistoryRequest request) {
        pushNotificationService.getHistory(request);
    }
}
