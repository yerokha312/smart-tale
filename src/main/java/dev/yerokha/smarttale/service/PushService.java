package dev.yerokha.smarttale.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PushService {

    private final SimpMessagingTemplate messagingTemplate;


    public PushService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendToUser(Long userId, Map<String, String> body) {
        messagingTemplate.convertAndSendToUser(userId.toString(), "/push", body);
    }

    public void sendToOrganization(Long organizationId, Map<String, String> body) {
        String destination = "/org/" + organizationId + "/push";
        messagingTemplate.convertAndSend(destination, body);
    }
}
