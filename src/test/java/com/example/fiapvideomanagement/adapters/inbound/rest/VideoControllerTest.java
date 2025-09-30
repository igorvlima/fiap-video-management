package com.example.fiapvideomanagement.adapters.inbound.rest;

import com.example.fiapvideomanagement.domain.model.Video;
import com.example.fiapvideomanagement.service.VideoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VideoController.class)
@Import(VideoControllerTest.TestConfig.class)
class VideoControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public VideoService videoService() {
            return Mockito.mock(VideoService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
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
    void findByCustomerEmail_ShouldReturnVideos() throws Exception {
        // Arrange
        when(videoService.findByCustomerEmail(customerEmail))
                .thenReturn(Arrays.asList(video));

        // Act & Assert
        mockMvc.perform(get("/api/v1/videos")
                        .param("customerEmail", customerEmail))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(videoId.toString())))
                .andExpect(jsonPath("$[0].name", is("Test Video")))
                .andExpect(jsonPath("$[0].url", is("s3://bucket/test-video.mp4")))
                .andExpect(jsonPath("$[0].customerId", is(1)))
                .andExpect(jsonPath("$[0].customerEmail", is(customerEmail)))
                .andExpect(jsonPath("$[0].status", is("PENDING")));

        verify(videoService).findByCustomerEmail(customerEmail);
    }

    @Test
    void getById_WhenVideoExists_ShouldReturnVideo() throws Exception {
        // Arrange
        when(videoService.findById(videoId)).thenReturn(Optional.of(video));

        // Act & Assert
        mockMvc.perform(get("/api/v1/videos/{id}", videoId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(videoId.toString())))
                .andExpect(jsonPath("$.name", is("Test Video")))
                .andExpect(jsonPath("$.url", is("s3://bucket/test-video.mp4")))
                .andExpect(jsonPath("$.customerId", is(1)))
                .andExpect(jsonPath("$.customerEmail", is(customerEmail)))
                .andExpect(jsonPath("$.status", is("PENDING")));

        verify(videoService).findById(videoId);
    }

    @Test
    void getById_WhenVideoDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(videoService.findById(videoId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/videos/{id}", videoId))
                .andExpect(status().isNotFound());

        verify(videoService).findById(videoId);
    }

    @Test
    void create_ShouldCreateVideoAndReturnOk() throws Exception {
        // Arrange
        doNothing().when(videoService).createVideo(any(Video.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/videos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(video)))
                .andExpect(status().isOk());

        verify(videoService).createVideo(any(Video.class));
    }
}