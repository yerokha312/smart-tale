/*
package dev.yerokha.smarttale.config;

import dev.yerokha.smarttale.exception.InvalidTokenException;
import dev.yerokha.smarttale.service.TokenService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class TokenChannelInterceptor implements ChannelInterceptor {

    private final TokenService tokenService;

    public TokenChannelInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String jwtToken = accessor.getFirstNativeHeader("Authorization");
            if (jwtToken != null && jwtToken.startsWith("Bearer ")) {
                jwtToken = jwtToken.substring(7);
                if (tokenService.validateToken(jwtToken)) {
                    Authentication auth = tokenService.getAuthentication(jwtToken);
                    accessor.setUser(auth);
                } else {
                    throw new InvalidTokenException("Invalid JWT token");
                }
            } else {
                throw new InvalidTokenException("Missing JWT token");
            }
        }

        return message;
    }
}
*/
