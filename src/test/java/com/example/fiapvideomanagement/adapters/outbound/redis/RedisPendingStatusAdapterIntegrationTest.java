package com.example.fiapvideomanagement.adapters.outbound.redis;

import com.example.fiapvideomanagement.domain.model.VideoStatusMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for RedisPendingStatusAdapter.
 * This test requires a Redis instance to be running.
 * Set the environment variable REDIS_INTEGRATION_TESTS=true to run these tests.
 */
@SpringBootTest
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "REDIS_INTEGRATION_TESTS", matches = "true")
class RedisPendingStatusAdapterIntegrationTest {

    @Autowired
    private RedisPendingStatusAdapter adapter;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private UUID videoId;
    private String videoIdStr;
    private VideoStatusMessage statusMessage;

    @BeforeEach
    void setUp() {
        // Clean up Redis before each test
        redisTemplate.getConnectionFactory().getConnection().flushAll();

        videoId = UUID.randomUUID();
        videoIdStr = videoId.toString();
        
        statusMessage = VideoStatusMessage.builder()
                .videoId(videoId)
                .videoName("Test Video")
                .customerEmail("test@example.com")
                .videoStatus("PROCESSED")
                .build();
    }

    @Test
    void savePendingStatus_ShouldSaveToRedis() {
        // Act
        adapter.savePendingStatus(videoIdStr, statusMessage);
        
        // Assert - We can't directly verify the expiration, but we can check that the value was saved
        String redisKey = "pending_status:" + videoIdStr;
        Object value = redisTemplate.opsForValue().get(redisKey);
        
        assertNotNull(value);
    }

    @Test
    void getAndRemovePendingStatus_WhenStatusExists_ShouldReturnAndRemoveStatus() {
        // Arrange
        adapter.savePendingStatus(videoIdStr, statusMessage);
        
        // Act
        Optional<VideoStatusMessage> result = adapter.getAndRemovePendingStatus(videoIdStr);
        
        // Assert
        assertTrue(result.isPresent());
        VideoStatusMessage retrievedMessage = result.get();
        assertEquals(videoId, retrievedMessage.getVideoId());
        assertEquals("Test Video", retrievedMessage.getVideoName());
        assertEquals("test@example.com", retrievedMessage.getCustomerEmail());
        assertEquals("PROCESSED", retrievedMessage.getVideoStatus());
        
        // Verify it was removed from Redis
        String redisKey = "pending_status:" + videoIdStr;
        Object value = redisTemplate.opsForValue().get(redisKey);
        assertNull(value);
    }

    @Test
    void getAndRemovePendingStatus_WhenStatusDoesNotExist_ShouldReturnEmpty() {
        // Act
        Optional<VideoStatusMessage> result = adapter.getAndRemovePendingStatus(videoIdStr);
        
        // Assert
        assertFalse(result.isPresent());
    }
}