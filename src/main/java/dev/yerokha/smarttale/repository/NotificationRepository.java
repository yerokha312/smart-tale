package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.entity.PushNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<PushNotification, Long> {
}
