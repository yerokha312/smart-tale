package dev.yerokha.smarttale.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.yerokha.smarttale.dto.PushNotification;
import dev.yerokha.smarttale.service.PushService;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Map;

@Aspect
@Component
public class NotificationAspect {

    private final PushService pushService;
    private final ObjectMapper objectMapper;

    public NotificationAspect(PushService pushService, ObjectMapper objectMapper) {
        this.pushService = pushService;
        this.objectMapper = objectMapper;
    }

    @AfterReturning(
            pointcut = "execution(* dev.yerokha.smarttale.service.AdvertisementService.acceptOrder(..))",
            returning = "acceptance"
    )
    public void afterAcceptOrder(PushNotification acceptance) throws JsonProcessingException {
        sendNotification(acceptance.id(), acceptance.data(), "user");
    }

    @AfterReturning(
            pointcut = "execution(* dev.yerokha.smarttale.service.OrganizationService.inviteEmployee(..))",
            returning = "invitation"
    )
    public void afterInviteEmployee(PushNotification invitation) throws JsonProcessingException {
        sendNotification(invitation.id(), invitation.data(), "user");
    }

    private void sendNotification(Long id, Map<String, String> data, String to) throws JsonProcessingException {
        if (to.equals("user")) {
            String body = objectMapper.writeValueAsString(data);
            pushService.sendToUser(id, body);
        } else if (to.equals("org")) {
            String body = objectMapper.writeValueAsString(data);
            pushService.sendToOrganization(id, body);
        } else {
            throw new UnsupportedOperationException("Unsupported notification type: " + to);
        }
    }
}
