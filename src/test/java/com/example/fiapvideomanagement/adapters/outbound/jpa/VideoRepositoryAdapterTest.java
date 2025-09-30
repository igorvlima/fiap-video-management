package com.example.fiapvideomanagement.adapters.outbound.jpa;

import com.example.fiapvideomanagement.domain.model.Video;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoRepositoryAdapterTest {

    @Mock
    private SpringVideoRepository springRepository;

    @InjectMocks
    private VideoRepositoryAdapter repositoryAdapter;

    private UUID videoId;
    private VideoEntity videoEntity;
    private String customerEmail;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        videoId = UUID.randomUUID();
        customerEmail = "test@example.com";
        now = LocalDateTime.now();
        
        videoEntity = VideoEntity.builder()
                .id(videoId)
                .name("Test Video")
                .url("s3://bucket/test-video.mp4")
                .customerId(1L)
                .customerEmail(customerEmail)
                .status("PENDING")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Test
    void save_ShouldConvertAndSaveVideo() {
        // Arrange
        Video video = Video.builder()
                .id(videoId)
                .name("Test Video")
                .url("s3://bucket/test-video.mp4")
                .customerId(1L)
                .customerEmail(customerEmail)
                .status("PENDING")
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(springRepository.save(any(VideoEntity.class))).thenReturn(videoEntity);

        // Act
        Video savedVideo = repositoryAdapter.save(video);

        // Assert
        assertNotNull(savedVideo);
        assertEquals(videoId, savedVideo.getId());
        assertEquals("Test Video", savedVideo.getName());
        assertEquals("s3://bucket/test-video.mp4", savedVideo.getUrl());
        assertEquals(1L, savedVideo.getCustomerId());
        assertEquals(customerEmail, savedVideo.getCustomerEmail());
        assertEquals("PENDING", savedVideo.getStatus());
        assertEquals(now, savedVideo.getCreatedAt());
        assertEquals(now, savedVideo.getUpdatedAt());

        verify(springRepository).save(any(VideoEntity.class));
    }

    @Test
    void findById_WhenVideoExists_ShouldReturnVideo() {
        // Arrange
        when(springRepository.findById(videoId)).thenReturn(Optional.of(videoEntity));

        // Act
        Optional<Video> result = repositoryAdapter.findById(videoId);

        // Assert
        assertTrue(result.isPresent());
        Video video = result.get();
        assertEquals(videoId, video.getId());
        assertEquals("Test Video", video.getName());
        assertEquals("s3://bucket/test-video.mp4", video.getUrl());
        assertEquals(1L, video.getCustomerId());
        assertEquals(customerEmail, video.getCustomerEmail());
        assertEquals("PENDING", video.getStatus());
        assertEquals(now, video.getCreatedAt());
        assertEquals(now, video.getUpdatedAt());

        verify(springRepository).findById(videoId);
    }

    @Test
    void findById_WhenVideoDoesNotExist_ShouldReturnEmptyOptional() {
        // Arrange
        when(springRepository.findById(videoId)).thenReturn(Optional.empty());

        // Act
        Optional<Video> result = repositoryAdapter.findById(videoId);

        // Assert
        assertFalse(result.isPresent());
        verify(springRepository).findById(videoId);
    }

    @Test
    void findByCustomerEmail_ShouldReturnListOfVideos() {
        // Arrange
        List<VideoEntity> videoEntities = Arrays.asList(videoEntity);
        when(springRepository.findByCustomerEmail(customerEmail)).thenReturn(videoEntities);

        // Act
        List<Video> videos = repositoryAdapter.findByCustomerEmail(customerEmail);

        // Assert
        assertNotNull(videos);
        assertEquals(1, videos.size());
        Video video = videos.get(0);
        assertEquals(videoId, video.getId());
        assertEquals("Test Video", video.getName());
        assertEquals("s3://bucket/test-video.mp4", video.getUrl());
        assertEquals(1L, video.getCustomerId());
        assertEquals(customerEmail, video.getCustomerEmail());
        assertEquals("PENDING", video.getStatus());
        assertEquals(now, video.getCreatedAt());
        assertEquals(now, video.getUpdatedAt());

        verify(springRepository).findByCustomerEmail(customerEmail);
    }

    @Test
    void existsById_ShouldDelegateToSpringRepository() {
        // Arrange
        when(springRepository.existsById(videoId)).thenReturn(true);

        // Act
        boolean result = repositoryAdapter.existsById(videoId);

        // Assert
        assertTrue(result);
        verify(springRepository).existsById(videoId);
    }
}