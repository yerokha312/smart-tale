package dev.yerokha.smarttale.config;

import dev.yerokha.smarttale.exception.InvalidTokenException;
import dev.yerokha.smarttale.service.TokenService;
import dev.yerokha.smarttale.util.UserConnectedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final TokenService tokenService;
    private final ApplicationEventPublisher eventPublisher;
    private final ThreadPoolTaskScheduler taskScheduler;
    private static final Map<Long, Boolean> onlineUsers = new ConcurrentHashMap<>();


    public WebSocketConfig(TokenService tokenService, ApplicationEventPublisher eventPublisher, ThreadPoolTaskScheduler taskScheduler) {
        this.tokenService = tokenService;
        this.eventPublisher = eventPublisher;
        this.taskScheduler = taskScheduler;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/user", "/org")
                .setTaskScheduler(taskScheduler)
                .setHeartbeatValue(new long[]{120000, 120000});
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://*", "https://*")
                .withSockJS();
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://*", "https://*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                assert accessor != null;
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String accessToken = accessor.getFirstNativeHeader("Authorization");
                    assert accessToken != null;
                    try {
                        Long userId = tokenService.getUserIdFromToken(accessToken);
                        Authentication user = tokenService.getAuthentication(accessToken);
                        accessor.setUser(user);
                        onlineUsers.put(userId, true);
                    } catch (Exception e) {
                        throw new InvalidTokenException("Invalid token on connect");
                    }
                } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                    String accessToken = accessor.getFirstNativeHeader("Authorization");
                    if (accessToken != null) {
                        Long userId = null;
                        try {
                            userId = tokenService.getUserIdFromTokenIgnoringExpiration(accessToken);
                            onlineUsers.remove(userId);
                        } finally {
                            if (userId != null) {
                                onlineUsers.remove(userId);
                            }
                        }
                    }
                }
                return message;
            }
        });
    }

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        GenericMessage<?> connectMessage = (GenericMessage<?>) headers.getHeader(SimpMessageHeaderAccessor.CONNECT_MESSAGE_HEADER);
        if (connectMessage != null) {
            StompHeaderAccessor connectHeaders = StompHeaderAccessor.wrap(connectMessage);
            String accessToken = connectHeaders.getFirstNativeHeader("Authorization");
            Long userId = tokenService.getUserIdFromToken(accessToken);
            if (userId != null) {
                eventPublisher.publishEvent(new UserConnectedEvent(this, userId));
            }
        }
    }

    public static boolean userIsOnline(Long userId) {
        return onlineUsers.containsKey(userId);
    }
}
