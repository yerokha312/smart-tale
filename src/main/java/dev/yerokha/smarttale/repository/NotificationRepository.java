package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.entity.PushNotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<PushNotificationEntity, Long> {

    List<PushNotificationEntity> findAllByRecipientIdAndIsSent(Long senderId, boolean isSent);
}
