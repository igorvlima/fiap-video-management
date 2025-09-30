package com.example.fiapvideomanagement.adapters.outbound.jpa;

import com.example.fiapvideomanagement.domain.model.Video;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(VideoRepositoryAdapter.class)
@ActiveProfiles("test")
class VideoRepositoryAdapterIntegrationTest {

    @Autowired
    private VideoRepositoryAdapter repositoryAdapter;

    @Autowired
    private SpringVideoRepository springRepository;

    private UUID videoId;
    private Video video;
    private String customerEmail;

    @BeforeEach
    void setUp() {
        // Clean up the database before each test
        springRepository.deleteAll();

        videoId = UUID.randomUUID();
        customerEmail = "test@example.com";
        LocalDateTime now = LocalDateTime.now();

        video = Video.builder()
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
    void save_ShouldPersistVideoToDatabase() {
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
        assertNotNull(savedVideo.getCreatedAt());
        assertNotNull(savedVideo.getUpdatedAt());

        // Verify it's in the database
        assertTrue(springRepository.existsById(videoId));
    }

    @Test
    void findById_WhenVideoExists_ShouldReturnVideo() {
        // Arrange
        repositoryAdapter.save(video);

        // Act
        Optional<Video> result = repositoryAdapter.findById(videoId);

        // Assert
        assertTrue(result.isPresent());
        Video foundVideo = result.get();
        assertEquals(videoId, foundVideo.getId());
        assertEquals("Test Video", foundVideo.getName());
        assertEquals("s3://bucket/test-video.mp4", foundVideo.getUrl());
        assertEquals(1L, foundVideo.getCustomerId());
        assertEquals(customerEmail, foundVideo.getCustomerEmail());
        assertEquals("PENDING", foundVideo.getStatus());
    }

    @Test
    void findById_WhenVideoDoesNotExist_ShouldReturnEmptyOptional() {
        // Act
        Optional<Video> result = repositoryAdapter.findById(UUID.randomUUID());

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findByCustomerEmail_ShouldReturnMatchingVideos() {
        // Arrange
        repositoryAdapter.save(video);

        // Create another video with a different email
        Video anotherVideo = Video.builder()
                .id(UUID.randomUUID())
                .name("Another Video")
                .url("s3://bucket/another-video.mp4")
                .customerId(2L)
                .customerEmail("another@example.com")
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        repositoryAdapter.save(anotherVideo);

        // Act
        List<Video> videos = repositoryAdapter.findByCustomerEmail(customerEmail);

        // Assert
        assertNotNull(videos);
        assertEquals(1, videos.size());
        Video foundVideo = videos.get(0);
        assertEquals(videoId, foundVideo.getId());
        assertEquals("Test Video", foundVideo.getName());
        assertEquals(customerEmail, foundVideo.getCustomerEmail());
    }

    @Test
    void existsById_ShouldReturnTrue_WhenVideoExists() {
        // Arrange
        repositoryAdapter.save(video);

        // Act
        boolean result = repositoryAdapter.existsById(videoId);

        // Assert
        assertTrue(result);
    }

    @Test
    void existsById_ShouldReturnFalse_WhenVideoDoesNotExist() {
        // Act
        boolean result = repositoryAdapter.existsById(UUID.randomUUID());

        // Assert
        assertFalse(result);
    }
}