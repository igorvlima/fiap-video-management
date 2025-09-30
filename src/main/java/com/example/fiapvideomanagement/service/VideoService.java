package com.example.fiapvideomanagement.service;

import com.example.fiapvideomanagement.domain.model.Video;
import com.example.fiapvideomanagement.domain.port.in.VideoUseCase;
import com.example.fiapvideomanagement.domain.port.out.VideoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoService implements VideoUseCase {

    private final VideoRepositoryPort repository;

    @Override
    @Transactional(readOnly = true)
    public List<Video> findByCustomerEmail(String customerEmail) {
        if (customerEmail == null || customerEmail.isBlank()) {
            throw new IllegalArgumentException("customerEmail is required");
        }
        return repository.findByCustomerEmail(customerEmail);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Video> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    @Transactional
    public void createVideo(Video video) {
        var now = LocalDateTime.now();
        if (video.getId() == null) video.setId(UUID.randomUUID());
        video.setCreatedAt(now);
        video.setUpdatedAt(now);
        repository.save(video);
    }

    @Override
    @Transactional
    public void updateVideoUrl(UUID id, String s3Key) {
        var opt = repository.findById(id);
        var video = opt.orElseThrow(() -> new IllegalArgumentException("Video not found"));
        video.setUrl(s3Key);
        video.setUpdatedAt(LocalDateTime.now());
        repository.save(video);
    }

    @Override
    @Transactional
    public void updateVideoStatus(UUID id, String status) {
        var opt = repository.findById(id);
        var video = opt.orElseThrow(() -> new IllegalArgumentException("Video not found"));
        video.setStatus(status);
        video.setUpdatedAt(LocalDateTime.now());
        repository.save(video);
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }
}
