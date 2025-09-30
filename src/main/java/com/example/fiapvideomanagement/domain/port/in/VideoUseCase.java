package com.example.fiapvideomanagement.domain.port.in;

import com.example.fiapvideomanagement.domain.model.Video;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VideoUseCase {
    List<Video> findByCustomerEmail(String customerEmail);
    Optional<Video> findById(UUID id);
    void createVideo(Video video);
    void updateVideoUrl(UUID id, String s3Key);
    void updateVideoStatus(UUID id, String status);
    boolean existsById(UUID id);
}
