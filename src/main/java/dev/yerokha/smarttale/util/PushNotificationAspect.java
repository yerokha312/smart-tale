package dev.yerokha.smarttale.util;

import dev.yerokha.smarttale.dto.PushNotification;
import dev.yerokha.smarttale.entity.user.PositionEntity;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.enums.RecipientType;
import dev.yerokha.smarttale.service.PushNotificationService;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dev.yerokha.smarttale.enums.RecipientType.ORGANIZATION;
import static dev.yerokha.smarttale.enums.RecipientType.USER;

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
        sendNotification(acceptance.recipientId(), acceptance.data(), USER);
    }

    @AfterReturning(
            pointcut = "execution(* dev.yerokha.smarttale.service.OrganizationService.inviteEmployee(..))",
            returning = "invitation"
    )
    public void afterInviteEmployee(PushNotification invitation) {
        sendNotification(invitation.recipientId(), invitation.data(), USER);
    }

    @AfterReturning(
            pointcut = "execution(* dev.yerokha.smarttale.service.AdvertisementService.confirmOrder(..))",
            returning = "confirmation"
    )
    public void afterConfirmOrder(PushNotification confirmation) {
        sendNotification(confirmation.recipientId(), confirmation.data(), ORGANIZATION);
    }

    @AfterReturning(
            pointcut = "execution(* dev.yerokha.smarttale.service.AdvertisementService.updateStatus(..))",
            returning = "notification"
    )
    public void afterChangeStatus(PushNotification notification) {
        sendNotification(notification.recipientId(), notification.data(), ORGANIZATION);
    }

    @AfterReturning(
            pointcut = "execution(* dev.yerokha.smarttale.service.OrganizationService.removeContractors(..))",
            returning = "notifications"
    )
    public void afterRemoveContractors(List<PushNotification> notifications) {
        for (PushNotification notification : notifications) {
            sendNotification(notification.recipientId(), notification.data(), USER);
        }
    }

    @AfterReturning(
            pointcut = "execution(* dev.yerokha.smarttale.service.OrganizationService.assignContractors(..))",
            returning = "notifications"
    )
    public void afterAssignContractors(List<PushNotification> notifications) {
        for (PushNotification notification : notifications) {
            sendNotification(notification.recipientId(), notification.data(), USER);
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
                    "title", position.getTitle()
            );
            sendNotification(employee.getUserId(), data, USER);
        }
        Map<String, String> data = Map.of(
                "sub", "Должность была обновлена",
                "posId", position.getPositionId().toString(),
                "title", position.getTitle()
        );
        sendNotification(position.getOrganization().getOrganizationId(), data, ORGANIZATION);
    }

    @AfterReturning(
            pointcut = "execution(* dev.yerokha.smarttale.service.OrganizationService.updateEmployee(..))",
            returning = "notification"
    )
    public void afterUpdateEmployee(PushNotification notification) {
        sendNotification(notification.recipientId(), notification.data(), USER);
    }

    @AfterReturning(
            pointcut = "execution(* dev.yerokha.smarttale.service.OrganizationService.deleteEmployee(..))",
            returning = "notification"
    )
    public void afterDeleteEmployee(PushNotification notification) {
        sendNotification(notification.recipientId(), notification.data(), USER);
    }

    @AfterReturning(pointcut = "execution(* dev.yerokha.smarttale.service.UserService.acceptInvitation(..))",
            returning = "notification")
    public void afterAcceptInvitation(PushNotification notification) {
        sendNotification(notification.recipientId(), notification.data(), ORGANIZATION);
    }


    private void sendNotification(Long id, Map<String, String> data, RecipientType to) {
        if (to.equals(USER)) {
            pushNotificationService.sendToUser(id, data);
        } else if (to.equals(ORGANIZATION)) {
            pushNotificationService.sendToOrganization(id, data);
        } else {
            throw new UnsupportedOperationException("Unsupported notification type: " + to);
        }
    }
}
