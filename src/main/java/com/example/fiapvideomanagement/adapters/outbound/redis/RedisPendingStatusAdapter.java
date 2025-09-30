package com.example.fiapvideomanagement.adapters.outbound.redis;

import com.example.fiapvideomanagement.domain.model.VideoStatusMessage;
import com.example.fiapvideomanagement.domain.port.out.PendingStatusStorePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
public class RedisPendingStatusAdapter implements PendingStatusStorePort {

    private static final String PENDING_STATUS_PREFIX = "pending_status:";
    private static final long STATUS_EXPIRATION_SECONDS = 600;

    private final RedisTemplate<String, VideoStatusMessage> redisTemplate;

    @Override
    public void savePendingStatus(String videoId, VideoStatusMessage message) {
        String redisKey = PENDING_STATUS_PREFIX + videoId;
        redisTemplate.opsForValue()
                .set(redisKey, message, STATUS_EXPIRATION_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public Optional<VideoStatusMessage> getAndRemovePendingStatus(String videoId) {
        String redisKey = PENDING_STATUS_PREFIX + videoId;
        VideoStatusMessage msg = redisTemplate.opsForValue().getAndDelete(redisKey);
        return Optional.ofNullable(msg);
    }
}
