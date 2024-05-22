package dev.yerokha.smarttale.entity;

import dev.yerokha.smarttale.util.MapToJsonConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications", indexes = @Index(name = "recipient_idx", columnList = "recipient_id"))
public class PushNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "recipient_id")
    private Long recipientId;

    @Column(name = "recipient_type")
    private String recipientType;

    @Convert(converter = MapToJsonConverter.class)
    @Column(name = "data")
    private Map<String, String> data;

    @Column(name = "is_sent")
    private boolean isSent;

    @Column(name = "is_read")
    private boolean isRead;


}
