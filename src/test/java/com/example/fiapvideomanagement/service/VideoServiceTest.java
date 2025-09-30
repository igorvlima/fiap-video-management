package com.example.fiapvideomanagement.service;

import com.example.fiapvideomanagement.domain.model.Video;
import com.example.fiapvideomanagement.domain.port.out.VideoRepositoryPort;
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
class VideoServiceTest {

    @Mock
    private VideoRepositoryPort repository;

    @InjectMocks
    private VideoService videoService;

    private UUID videoId;
    private Video video;
    private String customerEmail;

    @BeforeEach
    void setUp() {
        videoId = UUID.randomUUID();
        customerEmail = "test@example.com";
        video = Video.builder()
                .id(videoId)
                .name("Test Video")
                .url("s3://bucket/test-video.mp4")
                .customerId(1L)
                .customerEmail(customerEmail)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void findByCustomerEmail_ShouldReturnListOfVideos() {
        // Arrange
        List<Video> expectedVideos = Arrays.asList(video);
        when(repository.findByCustomerEmail(customerEmail)).thenReturn(expectedVideos);

        // Act
        List<Video> actualVideos = videoService.findByCustomerEmail(customerEmail);

        // Assert
        assertEquals(expectedVideos, actualVideos);
        verify(repository).findByCustomerEmail(customerEmail);
    }

    @Test
    void findByCustomerEmail_WithNullEmail_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> videoService.findByCustomerEmail(null));
        verify(repository, never()).findByCustomerEmail(any());
    }

    @Test
    void findByCustomerEmail_WithBlankEmail_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> videoService.findByCustomerEmail("  "));
        verify(repository, never()).findByCustomerEmail(any());
    }

    @Test
    void findById_ShouldReturnVideo() {
        // Arrange
        when(repository.findById(videoId)).thenReturn(Optional.of(video));

        // Act
        Optional<Video> result = videoService.findById(videoId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(video, result.get());
        verify(repository).findById(videoId);
    }

    @Test
    void findById_WithNonExistentId_ShouldReturnEmptyOptional() {
        // Arrange
        when(repository.findById(videoId)).thenReturn(Optional.empty());

        // Act
        Optional<Video> result = videoService.findById(videoId);

        // Assert
        assertFalse(result.isPresent());
        verify(repository).findById(videoId);
    }

    @Test
    void createVideo_ShouldSaveVideo() {
        // Arrange
        Video videoWithoutId = Video.builder()
                .name("Test Video")
                .url("s3://bucket/test-video.mp4")
                .customerId(1L)
                .customerEmail(customerEmail)
                .status("PENDING")
                .build();

        when(repository.save(any(Video.class))).thenReturn(video);

        // Act
        videoService.createVideo(videoWithoutId);

        // Assert
        verify(repository).save(any(Video.class));
        assertNotNull(videoWithoutId.getId());
        assertNotNull(videoWithoutId.getCreatedAt());
        assertNotNull(videoWithoutId.getUpdatedAt());
    }

    @Test
    void createVideo_WithExistingId_ShouldPreserveId() {
        // Arrange
        when(repository.save(any(Video.class))).thenReturn(video);

        // Act
        videoService.createVideo(video);

        // Assert
        verify(repository).save(video);
        assertEquals(videoId, video.getId());
        assertNotNull(video.getCreatedAt());
        assertNotNull(video.getUpdatedAt());
    }

    @Test
    void updateVideoUrl_ShouldUpdateUrlAndSave() {
        // Arrange
        String newUrl = "s3://bucket/updated-video.mp4";
        when(repository.findById(videoId)).thenReturn(Optional.of(video));
        when(repository.save(any(Video.class))).thenReturn(video);

        // Act
        videoService.updateVideoUrl(videoId, newUrl);

        // Assert
        assertEquals(newUrl, video.getUrl());
        verify(repository).findById(videoId);
        verify(repository).save(video);
    }

    @Test
    void updateVideoUrl_WithNonExistentId_ShouldThrowException() {
        // Arrange
        String newUrl = "s3://bucket/updated-video.mp4";
        when(repository.findById(videoId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> videoService.updateVideoUrl(videoId, newUrl));
        verify(repository).findById(videoId);
        verify(repository, never()).save(any(Video.class));
    }

    @Test
    void updateVideoStatus_ShouldUpdateStatusAndSave() {
        // Arrange
        String newStatus = "PROCESSED";
        when(repository.findById(videoId)).thenReturn(Optional.of(video));
        when(repository.save(any(Video.class))).thenReturn(video);

        // Act
        videoService.updateVideoStatus(videoId, newStatus);

        // Assert
        assertEquals(newStatus, video.getStatus());
        verify(repository).findById(videoId);
        verify(repository).save(video);
    }

    @Test
    void updateVideoStatus_WithNonExistentId_ShouldThrowException() {
        // Arrange
        String newStatus = "PROCESSED";
        when(repository.findById(videoId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> videoService.updateVideoStatus(videoId, newStatus));
        verify(repository).findById(videoId);
        verify(repository, never()).save(any(Video.class));
    }

    @Test
    void existsById_ShouldReturnTrue_WhenVideoExists() {
        // Arrange
        when(repository.existsById(videoId)).thenReturn(true);

        // Act
        boolean result = videoService.existsById(videoId);

        // Assert
        assertTrue(result);
        verify(repository).existsById(videoId);
    }

    @Test
    void existsById_ShouldReturnFalse_WhenVideoDoesNotExist() {
        // Arrange
        when(repository.existsById(videoId)).thenReturn(false);

        // Act
        boolean result = videoService.existsById(videoId);

        // Assert
        assertFalse(result);
        verify(repository).existsById(videoId);
    }
}