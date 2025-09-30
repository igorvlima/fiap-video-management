package com.example.fiapvideomanagement.adapters.outbound.jpa;

import com.example.fiapvideomanagement.domain.model.Video;
import com.example.fiapvideomanagement.domain.port.out.VideoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class VideoRepositoryAdapter implements VideoRepositoryPort {

    private final SpringVideoRepository springRepo;

    @Override
    public Video save(Video video) {
        VideoEntity entity = VideoEntity.builder()
                .id(video.getId())
                .name(video.getName())
                .url(video.getUrl())
                .customerId(video.getCustomerId())
                .customerEmail(video.getCustomerEmail())
                .status(video.getStatus())
                .createdAt(video.getCreatedAt())
                .updatedAt(video.getUpdatedAt())
                .build();

        var saved = springRepo.save(entity);
        return Video.builder()
                .id(saved.getId())
                .name(saved.getName())
                .url(saved.getUrl())
                .customerId(saved.getCustomerId())
                .customerEmail(saved.getCustomerEmail())
                .status(saved.getStatus())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }

    @Override
    public Optional<Video> findById(UUID id) {
        return springRepo.findById(id)
                .map(e -> Video.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .url(e.getUrl())
                        .customerId(e.getCustomerId())
                        .customerEmail(e.getCustomerEmail())
                        .status(e.getStatus())
                        .createdAt(e.getCreatedAt())
                        .updatedAt(e.getUpdatedAt())
                        .build());
    }

    @Override
    public List<Video> findByCustomerEmail(String customerEmail) {
        return springRepo.findByCustomerEmail(customerEmail)
                .stream()
                .map(e -> Video.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .url(e.getUrl())
                        .customerId(e.getCustomerId())
                        .customerEmail(e.getCustomerEmail())
                        .status(e.getStatus())
                        .createdAt(e.getCreatedAt())
                        .updatedAt(e.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(UUID id) {
        return springRepo.existsById(id);
    }
}
