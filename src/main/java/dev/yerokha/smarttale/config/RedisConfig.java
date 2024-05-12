package dev.yerokha.smarttale.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
//        ObjectMapper customMapper = objectMapper.copy()
//                .registerModule(
//                        new Hibernate6Module()
//                                .enable(Hibernate6Module.Feature.FORCE_LAZY_LOADING)
//                                .enable(Hibernate6Module.Feature.REPLACE_PERSISTENT_COLLECTIONS)
//                                .disable(Hibernate6Module.Feature.USE_TRANSIENT_ANNOTATION)
//                )
//                .activateDefaultTyping(
//                        objectMapper.getPolymorphicTypeValidator(),
//                        ObjectMapper.DefaultTyping.EVERYTHING,
//                        JsonTypeInfo.As.PROPERTY
//                );
        final RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}

