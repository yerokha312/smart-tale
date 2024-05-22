package dev.yerokha.smarttale.service;

import dev.yerokha.smarttale.repository.NotificationRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PushNotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;
//    private final RedisTemplate<String, >


    public PushNotificationService(SimpMessagingTemplate messagingTemplate, NotificationRepository notificationRepository) {
        this.messagingTemplate = messagingTemplate;
        this.notificationRepository = notificationRepository;
    }

    public void sendToUser(Long userId, Map<String, String> body) {
        messagingTemplate.convertAndSendToUser(userId.toString(), "/push", body);
    }

    public void sendToOrganization(Long organizationId, Map<String, String> body) {
        String destination = "/org/" + organizationId + "/push";
        messagingTemplate.convertAndSend(destination, body);
    }
}
