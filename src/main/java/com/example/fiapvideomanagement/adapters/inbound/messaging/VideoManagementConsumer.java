package com.example.fiapvideomanagement.adapters.inbound.messaging;

import com.example.fiapvideomanagement.domain.model.VideoMessage;
import com.example.fiapvideomanagement.domain.model.VideoStatusMessage;
import com.example.fiapvideomanagement.domain.port.in.VideoUseCase;
import com.example.fiapvideomanagement.domain.port.out.PendingStatusStorePort;
import com.example.fiapvideomanagement.mapper.Mapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class VideoManagementConsumer {

    private final ObjectMapper objectMapper;
    private final VideoUseCase videoUseCase;
    private final PendingStatusStorePort pendingStatusStorePort;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @KafkaListener(
            topics = "video-data",
            groupId = "video-management-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenVideoMessage(ConsumerRecord<String, String> record) {
        try {
            VideoMessage message = objectMapper.readValue(record.value(), VideoMessage.class);
            UUID videoId = UUID.fromString(message.getVideoId());
            log.info("Video received: {}", videoId);

            if (videoUseCase.existsById(videoId)) {
                videoUseCase.updateVideoUrl(videoId, message.getS3Key());
            } else {
                videoUseCase.createVideo(Mapper.toVideo(message));
            }

            scheduler.schedule(() -> {
                try {
                    pendingStatusStorePort.getAndRemovePendingStatus(videoId.toString())
                            .ifPresent(status -> {
                                log.info("Applying Pending Status to Video {}", videoId);
                                videoUseCase.updateVideoStatus(videoId, status.getVideoStatus());
                            });
                } catch (Exception e) {
                    log.error("Error applying scheduled pending status to video {}: {}", videoId, e.getMessage(), e);
                }
            }, 2, TimeUnit.SECONDS);

        } catch (Exception e) {
            log.error("Error processing Kafka message (video): {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
            topics = "video-status",
            groupId = "video-management-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenVideoStatusMessage(ConsumerRecord<String, String> record) {
        try {
            VideoStatusMessage message = objectMapper.readValue(record.value(), VideoStatusMessage.class);
            UUID videoId = message.getVideoId();
            log.info("Received status for video: {}", videoId);

            if (videoUseCase.existsById(videoId)) {
                log.info("Video already exists, apply status immediately.");
                videoUseCase.updateVideoStatus(videoId, message.getVideoStatus());
            } else {
                log.info("Video doesn't exist yet. Saving pending status in Redis.");
                pendingStatusStorePort.savePendingStatus(videoId.toString(), message);
            }

        } catch (Exception e) {
            log.error("Error processing Kafka message (status): {}", e.getMessage(), e);
        }
    }
}