package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.entity.PushNotificationEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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

    @Modifying
    @Query("UPDATE PushNotificationEntity n SET n.isSent = true WHERE n.notificationId = :notificationId")
    void markAsSent(Long notificationId);

    @Query("SELECT n FROM PushNotificationEntity n " +
           "WHERE (n.recipientId = :userId AND n.recipientType = 'USER') " +
           "OR (n.recipientId = :orgId AND n.recipientType = 'ORGANIZATION' AND :orgId > 0) " +
           "ORDER BY n.timestamp DESC")
    Slice<PushNotificationEntity> findHistory(Long userId, Long orgId, Pageable pageable);
}
