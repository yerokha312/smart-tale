package dev.yerokha.smarttale.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.yerokha.smarttale.dto.NotificationHistoryRequest;
import dev.yerokha.smarttale.dto.NotificationSliceContainer;
import dev.yerokha.smarttale.entity.PushNotificationEntity;
import dev.yerokha.smarttale.repository.NotificationRepository;
import dev.yerokha.smarttale.repository.UserDetailsRepository;
import dev.yerokha.smarttale.util.UserConnectedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static dev.yerokha.smarttale.config.WebSocketConfig.userIsOnline;
import static dev.yerokha.smarttale.enums.RecipientType.ORGANIZATION;
import static dev.yerokha.smarttale.enums.RecipientType.USER;

@Service
public class PushNotificationService {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final UserDetailsRepository userDetailsRepository;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ObjectMapper objectMapper;

    public PushNotificationService(SimpMessagingTemplate messagingTemplate,
                                   NotificationRepository notificationRepository,
                                   RedisTemplate<String, String> redisTemplate,
                                   UserDetailsRepository userDetailsRepository,
                                   ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.notificationRepository = notificationRepository;
        this.redisTemplate = redisTemplate;
        this.userDetailsRepository = userDetailsRepository;
        this.objectMapper = objectMapper;
    }

    public void sendToUser(Long userId, Map<String, String> body) {
        PushNotificationEntity pushNotificationEntity = new PushNotificationEntity(
                userId,
                USER,
                objectMapper.valueToTree(body),
                Instant.now(),
                userIsOnline(userId)
        );

        pushNotificationEntity = notificationRepository.save(pushNotificationEntity);
        if (pushNotificationEntity.isSent()) {
            messagingTemplate.convertAndSendToUser(userId.toString(), "/push", pushNotificationEntity);
        } else {
            queueNotification(userId, pushNotificationEntity);
        }
    }

    public void sendToOrganization(Long organizationId, Map<String, String> body) {
        PushNotificationEntity pushNotificationEntity = new PushNotificationEntity(
                organizationId,
                ORGANIZATION,
                objectMapper.valueToTree(body),
                Instant.now(),
                true
        );
        notificationRepository.save(pushNotificationEntity);
        String destination = "/org/" + organizationId + "/push";
        messagingTemplate.convertAndSend(destination, pushNotificationEntity);
        List<Long> employeeIdList = userDetailsRepository.findAllUserIdsByOrganizationId(organizationId);
        for (Long employeeId : employeeIdList) {
            if (!userIsOnline(employeeId)) {
                queueNotification(employeeId, pushNotificationEntity);
            }
        }
    }

    private void queueNotification(Long userId, PushNotificationEntity body) {
        try {
            redisTemplate.opsForList().leftPush(userId + "-notifications", objectMapper.writeValueAsString(body));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not serialize push notification", e);
        }
    }

    @Transactional
    public void sendQueuedNotification(Long userId) {
        // Retrieve and delete Redis notifications
        List<String> queuedRedisNotifications = getQueuedRedisNotifications(userId);

        // Retrieve unsent database notifications and mark them as sent
        List<PushNotificationEntity> unsentDatabaseNotifications = getUnsentDatabaseNotifications(userId);
        markNotificationsAsSent(unsentDatabaseNotifications);

        // Combine all notifications to Set, convert back to List sorted by timestamp desc and send
        List<PushNotificationEntity> notificationsToSend = combineNotifications(queuedRedisNotifications, unsentDatabaseNotifications);

        // Send notifications
        sendNotifications(userId, notificationsToSend);
    }

    private List<String> getQueuedRedisNotifications(Long userId) {
        List<String> queuedNotifications = redisTemplate.opsForList().range(userId + "-notifications", 0, -1);
        if (queuedNotifications != null && !queuedNotifications.isEmpty()) {
            redisTemplate.delete(userId + "-notifications");
        }
        return queuedNotifications != null ? queuedNotifications : new ArrayList<>();
    }

    private List<PushNotificationEntity> getUnsentDatabaseNotifications(Long userId) {
        return notificationRepository.findAllByRecipientIdAndIsSent(userId, false);
    }

    private List<PushNotificationEntity> combineNotifications(List<String> redisNotifications,
                                                              List<PushNotificationEntity> dbNotifications) {
        Set<PushNotificationEntity> combinedNotificationsSet = new HashSet<>();

        combinedNotificationsSet.addAll(redisNotifications.stream()
                .map(n -> {
                    try {
                        return objectMapper.readValue(n, PushNotificationEntity.class);
                    } catch (JsonProcessingException e) {
                        throw new IllegalArgumentException("Could not deserialize push notification", e);
                    }
                })
                .collect(Collectors.toSet()));
        combinedNotificationsSet.addAll(dbNotifications);

        List<PushNotificationEntity> combinedNotifications = new ArrayList<>(combinedNotificationsSet);
        combinedNotifications.sort((n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp()));

        return combinedNotifications;
    }

    private void sendNotifications(Long userId, List<PushNotificationEntity> notificationsToSend) {
        notificationsToSend.forEach(n ->
                messagingTemplate.convertAndSendToUser(userId.toString(), "/push", n));
    }

    private void markNotificationsAsSent(List<PushNotificationEntity> unsentNotifications) {
        unsentNotifications.forEach(n -> {
            if (!n.isSent()) {
                n.setSent(true);
                notificationRepository.markAsSent(n.getNotificationId());
            }
        });
    }

    @EventListener
    @Transactional
    public void handleUserConnected(UserConnectedEvent event) {
        scheduler.schedule(() -> sendQueuedNotification(event.getUserId()), 1, TimeUnit.SECONDS);
        log.info("User {} connected", event.getUserId());
    }

    @Transactional
    public void markNotificationAsRead(Long notificationId) {
        notificationRepository.markAsRead(notificationId);
    }

    @Transactional
    public void getHistory(NotificationHistoryRequest request) {
        Pageable pageable = PageRequest.of(request.page(), request.size());
        Slice<PushNotificationEntity> history = notificationRepository
                .findHistory(request.userId(), request.organizationId(), pageable);

        List<PushNotificationEntity> content = history.getContent();
        NotificationSliceContainer container = new NotificationSliceContainer(
                content,
                history.hasNext()
        );
        markNotificationsAsSent(content);
        messagingTemplate.convertAndSendToUser(request.userId().toString(), "/push", container);
    }
}
