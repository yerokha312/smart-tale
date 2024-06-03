package dev.yerokha.smarttale.service;

import dev.yerokha.smarttale.entity.PushNotificationEntity;
import dev.yerokha.smarttale.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TransactionalNotificationService {

    private final NotificationRepository notificationRepository;

    public TransactionalNotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void markNotificationsAsSent(List<PushNotificationEntity> unsentNotifications) {
        unsentNotifications.forEach(n -> {
            if (!n.isSent()) {
                n.setSent(true);
                notificationRepository.markAsSent(n.getNotificationId());
            }
        });
    }
}
