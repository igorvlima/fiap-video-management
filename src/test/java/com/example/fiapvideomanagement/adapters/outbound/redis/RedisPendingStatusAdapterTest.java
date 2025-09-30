package com.example.fiapvideomanagement.adapters.outbound.redis;

import com.example.fiapvideomanagement.domain.model.VideoStatusMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisPendingStatusAdapterTest {

    private static final String PENDING_STATUS_PREFIX = "pending_status:";
    private static final long STATUS_EXPIRATION_SECONDS = 600;

    @Mock
    private RedisTemplate<String, VideoStatusMessage> redisTemplate;

    @Mock
    private ValueOperations<String, VideoStatusMessage> valueOperations;

    @InjectMocks
    private RedisPendingStatusAdapter adapter;

    private UUID videoId;
    private String videoIdStr;
    private String redisKey;
    private VideoStatusMessage statusMessage;

    @BeforeEach
    void setUp() {
        videoId = UUID.randomUUID();
        videoIdStr = videoId.toString();
        redisKey = PENDING_STATUS_PREFIX + videoIdStr;
        
        statusMessage = VideoStatusMessage.builder()
                .videoId(videoId)
                .videoName("Test Video")
                .customerEmail("test@example.com")
                .videoStatus("PROCESSED")
                .build();
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void savePendingStatus_ShouldSaveToRedisWithExpiration() {
        // Act
        adapter.savePendingStatus(videoIdStr, statusMessage);
        
        // Assert
        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(redisKey, statusMessage, STATUS_EXPIRATION_SECONDS, TimeUnit.SECONDS);
    }

    @Test
    void getAndRemovePendingStatus_WhenStatusExists_ShouldReturnStatus() {
        // Arrange
        when(valueOperations.getAndDelete(redisKey)).thenReturn(statusMessage);
        
        // Act
        Optional<VideoStatusMessage> result = adapter.getAndRemovePendingStatus(videoIdStr);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(statusMessage, result.get());
        verify(redisTemplate).opsForValue();
        verify(valueOperations).getAndDelete(redisKey);
    }

    @Test
    void getAndRemovePendingStatus_WhenStatusDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(valueOperations.getAndDelete(redisKey)).thenReturn(null);
        
        // Act
        Optional<VideoStatusMessage> result = adapter.getAndRemovePendingStatus(videoIdStr);
        
        // Assert
        assertFalse(result.isPresent());
        verify(redisTemplate).opsForValue();
        verify(valueOperations).getAndDelete(redisKey);
    }
}