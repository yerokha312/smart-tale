package dev.yerokha.smarttale.util;

import dev.yerokha.smarttale.entity.PushNotification;
import dev.yerokha.smarttale.entity.user.PositionEntity;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.service.PushNotificationService;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Aspect
@Component
public class PushNotificationAspect {

    private final PushNotificationService pushNotificationService;

    public PushNotificationAspect(PushNotificationService pushNotificationService) {
        this.pushNotificationService = pushNotificationService;
    }

    @AfterReturning(
            pointcut = "execution(* dev.yerokha.smarttale.service.AdvertisementService.acceptOrder(..))",
            returning = "acceptance"
    )
    public void afterAcceptOrder(PushNotification acceptance) {
        sendNotification(acceptance.recipientId(), acceptance.data(), "user");
    }

    @AfterReturning(
            pointcut = "execution(* dev.yerokha.smarttale.service.OrganizationService.inviteEmployee(..))",
            returning = "invitation"
    )
    public void afterInviteEmployee(PushNotification invitation) {
        sendNotification(invitation.recipientId(), invitation.data(), "user");
    }

    @AfterReturning(
            pointcut = "execution(* dev.yerokha.smarttale.service.AdvertisementService.confirmOrder(..))",
            returning = "confirmation"
    )
    public void afterConfirmOrder(PushNotification confirmation) {
        sendNotification(confirmation.recipientId(), confirmation.data(), "org");
    }

    @AfterReturning(
            pointcut = "execution(* dev.yerokha.smarttale.service.AdvertisementService.updateStatus(..))",
            returning = "notification"
    )
    public void afterChangeStatus(PushNotification notification) {
        sendNotification(notification.recipientId(), notification.data(), "org");
    }

    @AfterReturning(
            pointcut = "execution(* dev.yerokha.smarttale.service.OrganizationService.removeContractors(..))",
            returning = "notifications"
    )
    public void afterRemoveContractors(List<PushNotification> notifications) {
        for (PushNotification notification : notifications) {
            sendNotification(notification.recipientId(), notification.data(), "user");
        }
    }

    @AfterReturning(
            pointcut = "execution(* dev.yerokha.smarttale.service.OrganizationService.assignContractors(..))",
            returning = "notifications"
    )
    public void afterAssignContractors(List<PushNotification> notifications) {
        for (PushNotification notification : notifications) {
            sendNotification(notification.recipientId(), notification.data(), "user");
        }
    }

    @AfterReturning(
            pointcut = "execution(* dev.yerokha.smarttale.service.OrganizationService.updatePosition(..))",
            returning = "position"
    )
    public void afterUpdatePosition(PositionEntity position) {
        List<UserDetailsEntity> employees = new ArrayList<>(position.getEmployees());
        for (UserDetailsEntity employee : employees) {
            Map<String, String> data = Map.of(
                    "sub", "Ваша должность обновлена",
                    "posId", position.getPositionId().toString(),
                    "title", position.getTitle(),
                    "timestamp", Instant.now().toString()
            );
            sendNotification(employee.getUserId(), data, "user");
        }
        Map<String, String> data = Map.of(
                "sub", "Должность была обновлена",
                "posId", position.getPositionId().toString(),
                "title", position.getTitle(),
                "timestamp", Instant.now().toString()
        );
        sendNotification(position.getOrganization().getOrganizationId(), data, "org");
    }

    @AfterReturning(
            pointcut = "execution(* dev.yerokha.smarttale.service.OrganizationService.updateEmployee(..))",
            returning = "notification"
    )
    public void afterUpdateEmployee(PushNotification notification) {
        sendNotification(notification.recipientId(), notification.data(), "user");
    }

    private void sendNotification(Long id, Map<String, String> data, String to) {
        if (to.equals("user")) {
            pushNotificationService.sendToUser(id, data);
        } else if (to.equals("org")) {
            pushNotificationService.sendToOrganization(id, data);
        } else {
            throw new UnsupportedOperationException("Unsupported notification type: " + to);
        }
    }
}
