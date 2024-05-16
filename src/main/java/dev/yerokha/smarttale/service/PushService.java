package dev.yerokha.smarttale.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class PushService {

    private final SimpMessagingTemplate messagingTemplate;


    public PushService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendToUser(Long userId, String body) {
        messagingTemplate.convertAndSendToUser(userId.toString(), "/notifications/user", body);
    }

    public void sendToOrganization(Long organizationId, String body) {
        String destination = "/notifications/org/" + organizationId;
        messagingTemplate.convertAndSend(destination, body);
    }
}
