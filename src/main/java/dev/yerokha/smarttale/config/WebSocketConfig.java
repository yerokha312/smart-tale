package dev.yerokha.smarttale.config;

import dev.yerokha.smarttale.exception.InvalidTokenException;
import dev.yerokha.smarttale.service.TokenService;
import dev.yerokha.smarttale.util.UserConnectedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Map<Long, Boolean> onlineUsers = new ConcurrentHashMap<>();

    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

    public WebSocketConfig(TokenService tokenService, ApplicationEventPublisher eventPublisher) {
        this.tokenService = tokenService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/user", "/org");
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
                log.info("Headers: {}", accessor);
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
                        log.error("Invalid token on connect {}", e.getMessage());
                        throw new InvalidTokenException("Invalid token on connect");
                    }
                } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                    String accessToken = accessor.getFirstNativeHeader("Authorization");
                    if (accessToken != null) {
                        Long userId = null;
                        try {
                            userId = tokenService.getUserIdFromTokenIgnoringExpiration(accessToken);
                            onlineUsers.remove(userId);
                        } catch (Exception e) {
                            log.error("Invalid token on disconnect");
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
            log.info("Authorization header: {}", accessToken);
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
