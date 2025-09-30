package com.example.fiapvideomanagement.e2e;

import com.example.fiapvideomanagement.domain.model.Video;
import com.example.fiapvideomanagement.domain.model.VideoMessage;
import com.example.fiapvideomanagement.domain.model.VideoStatusMessage;
import com.example.fiapvideomanagement.service.VideoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end test for the Video Management application.
 * This test verifies the complete workflow of the application.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VideoManagementE2ETest {

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
    void testCreateAndRetrieveVideo() throws Exception {
        // 1. Create a video via REST API
        mockMvc.perform(post("/api/v1/videos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(video)))
                .andExpect(status().isOk());

        // 2. Retrieve the video by ID
        MvcResult result = mockMvc.perform(get("/api/v1/videos/{id}", videoId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(videoId.toString()))
                .andExpect(jsonPath("$.name").value("Test Video"))
                .andExpect(jsonPath("$.url").value("s3://bucket/test-video.mp4"))
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.customerEmail").value(customerEmail))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        // 3. Retrieve videos by customer email
        mockMvc.perform(get("/api/v1/videos")
                        .param("customerEmail", customerEmail))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(videoId.toString()))
                .andExpect(jsonPath("$[0].name").value("Test Video"))
                .andExpect(jsonPath("$[0].customerEmail").value(customerEmail));

        // 4. Update video status directly via service
        videoService.updateVideoStatus(videoId, "PROCESSED");

        // 5. Verify the status was updated
        mockMvc.perform(get("/api/v1/videos/{id}", videoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESSED"));
    }

    @Test
    void testVideoNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        
        mockMvc.perform(get("/api/v1/videos/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testInvalidCustomerEmail() throws Exception {
        mockMvc.perform(get("/api/v1/videos")
                        .param("customerEmail", ""))
                .andExpect(status().isBadRequest());
    }
}