package com.example.fiapvideomanagement.adapters.inbound.messaging;

import com.example.fiapvideomanagement.domain.model.Video;
import com.example.fiapvideomanagement.domain.model.VideoMessage;
import com.example.fiapvideomanagement.domain.model.VideoStatusMessage;
import com.example.fiapvideomanagement.domain.port.in.VideoUseCase;
import com.example.fiapvideomanagement.domain.port.out.PendingStatusStorePort;
import com.example.fiapvideomanagement.mapper.Mapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoManagementConsumerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private VideoUseCase videoUseCase;

    @Mock
    private PendingStatusStorePort pendingStatusStorePort;

    @Mock
    private ScheduledExecutorService scheduler;

    @InjectMocks
    private VideoManagementConsumer consumer;

    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;

    private UUID videoId;
    private VideoMessage videoMessage;
    private VideoStatusMessage videoStatusMessage;
    private Video video;
    private ConsumerRecord<String, String> videoRecord;
    private ConsumerRecord<String, String> statusRecord;
    private String videoMessageJson;
    private String statusMessageJson;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        videoId = UUID.randomUUID();
        String videoIdStr = videoId.toString();
        
        // Set up VideoMessage
        videoMessage = VideoMessage.builder()
                .videoId(videoIdStr)
                .s3Key("s3://bucket/test-video.mp4")
                .originalName("original.mp4")
                .generatedName("generated.mp4")
                .customerId(1L)
                .customerEmail("test@example.com")
                .build();
        
        videoMessageJson = "{\"videoId\":\"" + videoIdStr + "\",\"s3Key\":\"s3://bucket/test-video.mp4\",\"originalName\":\"original.mp4\",\"generatedName\":\"generated.mp4\",\"customerId\":1,\"customerEmail\":\"test@example.com\"}";
        videoRecord = new ConsumerRecord<>("video-data", 0, 0, "key", videoMessageJson);
        
        // Set up VideoStatusMessage
        videoStatusMessage = VideoStatusMessage.builder()
                .videoId(videoId)
                .videoName("Test Video")
                .customerEmail("test@example.com")
                .videoStatus("PROCESSED")
                .build();
        
        statusMessageJson = "{\"videoId\":\"" + videoIdStr + "\",\"videoName\":\"Test Video\",\"customerEmail\":\"test@example.com\",\"videoStatus\":\"PROCESSED\"}";
        statusRecord = new ConsumerRecord<>("video-status", 0, 0, "key", statusMessageJson);
        
        // Set up Video
        video = Video.builder()
                .id(videoId)
                .name("original.mp4")
                .url("s3://bucket/test-video.mp4")
                .customerId(1L)
                .customerEmail("test@example.com")
                .build();
        
        // Mock ObjectMapper
        when(objectMapper.readValue(videoMessageJson, VideoMessage.class)).thenReturn(videoMessage);
        when(objectMapper.readValue(statusMessageJson, VideoStatusMessage.class)).thenReturn(videoStatusMessage);
        
        // Replace the scheduler with our mock
        try (MockedStatic<java.util.concurrent.Executors> executorsMock = Mockito.mockStatic(java.util.concurrent.Executors.class)) {
            executorsMock.when(java.util.concurrent.Executors::newSingleThreadScheduledExecutor).thenReturn(scheduler);
            consumer = new VideoManagementConsumer(objectMapper, videoUseCase, pendingStatusStorePort);
        }
    }

    @Test
    void listenVideoMessage_WhenVideoExists_ShouldUpdateUrl() throws Exception {
        // Arrange
        when(videoUseCase.existsById(videoId)).thenReturn(true);
        
        // Act
        consumer.listenVideoMessage(videoRecord);
        
        // Assert
        verify(videoUseCase).existsById(videoId);
        verify(videoUseCase).updateVideoUrl(videoId, "s3://bucket/test-video.mp4");
        verify(videoUseCase, never()).createVideo(any(Video.class));
        verify(scheduler).schedule(any(Runnable.class), eq(2L), eq(TimeUnit.SECONDS));
    }

    @Test
    void listenVideoMessage_WhenVideoDoesNotExist_ShouldCreateVideo() throws Exception {
        // Arrange
        when(videoUseCase.existsById(videoId)).thenReturn(false);
        
        // Act
        consumer.listenVideoMessage(videoRecord);
        
        // Assert
        verify(videoUseCase).existsById(videoId);
        verify(videoUseCase, never()).updateVideoUrl(any(UUID.class), anyString());
        verify(videoUseCase).createVideo(any(Video.class));
        verify(scheduler).schedule(any(Runnable.class), eq(2L), eq(TimeUnit.SECONDS));
    }

    @Test
    void listenVideoMessage_ShouldScheduleCheckForPendingStatus() throws Exception {
        // Arrange
        when(videoUseCase.existsById(videoId)).thenReturn(true);
        
        // Act
        consumer.listenVideoMessage(videoRecord);
        
        // Assert
        verify(scheduler).schedule(runnableCaptor.capture(), eq(2L), eq(TimeUnit.SECONDS));
        
        // Simulate running the scheduled task
        when(pendingStatusStorePort.getAndRemovePendingStatus(videoId.toString()))
                .thenReturn(Optional.of(videoStatusMessage));
        
        runnableCaptor.getValue().run();
        
        verify(pendingStatusStorePort).getAndRemovePendingStatus(videoId.toString());
        verify(videoUseCase).updateVideoStatus(videoId, "PROCESSED");
    }

    @Test
    void listenVideoStatusMessage_WhenVideoExists_ShouldUpdateStatusImmediately() throws Exception {
        // Arrange
        when(videoUseCase.existsById(videoId)).thenReturn(true);
        
        // Act
        consumer.listenVideoStatusMessage(statusRecord);
        
        // Assert
        verify(videoUseCase).existsById(videoId);
        verify(videoUseCase).updateVideoStatus(videoId, "PROCESSED");
        verify(pendingStatusStorePort, never()).savePendingStatus(anyString(), any(VideoStatusMessage.class));
    }

    @Test
    void listenVideoStatusMessage_WhenVideoDoesNotExist_ShouldSavePendingStatus() throws Exception {
        // Arrange
        when(videoUseCase.existsById(videoId)).thenReturn(false);
        
        // Act
        consumer.listenVideoStatusMessage(statusRecord);
        
        // Assert
        verify(videoUseCase).existsById(videoId);
        verify(videoUseCase, never()).updateVideoStatus(any(UUID.class), anyString());
        verify(pendingStatusStorePort).savePendingStatus(videoId.toString(), videoStatusMessage);
    }

    @Test
    void listenVideoMessage_WhenExceptionOccurs_ShouldHandleGracefully() throws Exception {
        // Arrange
        when(objectMapper.readValue(anyString(), eq(VideoMessage.class)))
                .thenThrow(new JsonProcessingException("Test exception") {});
        
        // Act - should not throw exception
        consumer.listenVideoMessage(videoRecord);
        
        // Assert - no interactions with other dependencies
        verify(videoUseCase, never()).existsById(any(UUID.class));
        verify(videoUseCase, never()).updateVideoUrl(any(UUID.class), anyString());
        verify(videoUseCase, never()).createVideo(any(Video.class));
    }

    @Test
    void listenVideoStatusMessage_WhenExceptionOccurs_ShouldHandleGracefully() throws Exception {
        // Arrange
        when(objectMapper.readValue(anyString(), eq(VideoStatusMessage.class)))
                .thenThrow(new JsonProcessingException("Test exception") {});
        
        // Act - should not throw exception
        consumer.listenVideoStatusMessage(statusRecord);
        
        // Assert - no interactions with other dependencies
        verify(videoUseCase, never()).existsById(any(UUID.class));
        verify(videoUseCase, never()).updateVideoStatus(any(UUID.class), anyString());
        verify(pendingStatusStorePort, never()).savePendingStatus(anyString(), any(VideoStatusMessage.class));
    }
}