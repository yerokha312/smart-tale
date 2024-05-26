package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.entity.PushNotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<PushNotificationEntity, Long> {

    List<PushNotificationEntity> findAllByRecipientIdAndIsSent(Long senderId, boolean isSent);

    @Modifying
    @Query("UPDATE PushNotificationEntity n SET n.isRead = true WHERE n.notificationId = :notificationId")
    void markAsRead(Long notificationId);
}
