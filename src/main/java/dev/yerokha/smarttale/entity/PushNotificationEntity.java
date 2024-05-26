package dev.yerokha.smarttale.entity;

import com.fasterxml.jackson.databind.JsonNode;
import dev.yerokha.smarttale.enums.RecipientType;
import dev.yerokha.smarttale.util.JsonNodeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.Instant;
import java.util.Objects;

@Data
@Entity
@Table(name = "notifications", indexes = @Index(name = "recipient_idx", columnList = "recipient_id"))
public class PushNotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notifications_notification_id_seq")
    @SequenceGenerator(name = "notifications_notification_id_seq", allocationSize = 5)
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_type", nullable = false)
    private RecipientType recipientType;

    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "data", nullable = false, columnDefinition = "jsonb")
    private JsonNode data;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "is_sent", nullable = false)
    private boolean isSent;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    public PushNotificationEntity() {
    }

    public PushNotificationEntity(Long recipientId,
                                  RecipientType recipientType,
                                  JsonNode data,
                                  Instant timestamp,
                                  boolean isSent) {
        this.recipientId = recipientId;
        this.recipientType = recipientType;
        this.data = data;
        this.timestamp = timestamp;
        this.isSent = isSent;
        this.isRead = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PushNotificationEntity that = (PushNotificationEntity) o;
        return Objects.equals(notificationId, that.notificationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId);
    }
}

