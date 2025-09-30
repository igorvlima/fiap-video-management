package com.example.fiapvideomanagement.config;

import com.example.fiapvideomanagement.domain.model.VideoStatusMessage;
import com.example.fiapvideomanagement.domain.port.out.PendingStatusStorePort;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Test configuration that provides mock implementations of external dependencies.
 */
@TestConfiguration
@Profile("test")
public class TestConfig {

    /**
     * Provides a mock implementation of PendingStatusStorePort that uses an in-memory map
     * instead of Redis.
     */
    @Bean
    @Primary
    public PendingStatusStorePort mockPendingStatusStorePort() {
        return new PendingStatusStorePort() {
            private final Map<String, VideoStatusMessage> pendingStatuses = new ConcurrentHashMap<>();

            @Override
            public void savePendingStatus(String videoId, VideoStatusMessage message) {
                pendingStatuses.put(videoId, message);
            }

            @Override
            public Optional<VideoStatusMessage> getAndRemovePendingStatus(String videoId) {
                VideoStatusMessage message = pendingStatuses.remove(videoId);
                return Optional.ofNullable(message);
            }
        };
    }

    /**
     * Provides a mock RedisConnectionFactory that doesn't actually connect to Redis.
     */
    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        // This is a mock connection factory that doesn't actually connect to Redis
        return new LettuceConnectionFactory();
    }

    /**
     * Provides a RedisTemplate that uses the mock connection factory.
     */
    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        return template;
    }
}