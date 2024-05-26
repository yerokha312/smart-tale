package dev.yerokha.smarttale.config;

import dev.yerokha.smarttale.entity.user.UserEntity;
import dev.yerokha.smarttale.exception.InvalidTokenException;
import dev.yerokha.smarttale.service.TokenService;
import dev.yerokha.smarttale.service.UserService;
import dev.yerokha.smarttale.util.UserConnectedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;
    private static final Map<Long, Boolean> onlineUsers = new ConcurrentHashMap<>();

    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

    public WebSocketConfig(TokenService tokenService, UserService userService, ApplicationEventPublisher eventPublisher) {
        this.tokenService = tokenService;
        this.userService = userService;
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
                        String username = tokenService.getEmailFromToken(accessToken);
                        UserDetails userDetails = userService.loadUserByUsername(username);
                        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                        onlineUsers.put(userId, true);
                        accessor.setUser(usernamePasswordAuthenticationToken);
                    } catch (Exception e) {
                        log.error("Invalid token on connect " + e.getMessage());
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
        UsernamePasswordAuthenticationToken userToken = (UsernamePasswordAuthenticationToken) headers.getUser();
        if (userToken != null && userToken.getPrincipal() instanceof UserDetails userDetails) {
            Long userId = ((UserEntity) userDetails).getUserId();
            if (userId != null) {
                eventPublisher.publishEvent(new UserConnectedEvent(this, userId));
            }
        }
    }

    public static boolean userIsOnline(Long userId) {
        return onlineUsers.containsKey(userId);
    }
}
