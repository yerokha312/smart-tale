package dev.yerokha.smarttale.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
/*
* */
@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {
    // TODO temporary solution wait for Spring team to disable CSRF-token in @EnableWebSocketSecurity annotation
    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
                .simpSubscribeDestMatchers("/user/**").authenticated()
                .simpSubscribeDestMatchers("/org/**").hasAuthority("EMPLOYEE");
    }

    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }
}