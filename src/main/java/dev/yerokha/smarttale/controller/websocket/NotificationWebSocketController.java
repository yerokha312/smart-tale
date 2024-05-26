package dev.yerokha.smarttale.controller.websocket;

import dev.yerokha.smarttale.service.PushNotificationService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationWebSocketController {

    private final PushNotificationService pushNotificationService;

    public NotificationWebSocketController(PushNotificationService pushNotificationService) {
        this.pushNotificationService = pushNotificationService;
    }

    @MessageMapping({"/notifications/markAsRead"})
    @SendTo({"/topic/notifications"})
    public void markAsRead(@Payload Long notificationId) {
        pushNotificationService.markAsRead(notificationId);
    }
}
