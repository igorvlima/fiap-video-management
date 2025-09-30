package com.example.fiapvideomanagement.mapper;


import com.example.fiapvideomanagement.adapters.outbound.jpa.VideoEntity;
import com.example.fiapvideomanagement.domain.model.Video;
import com.example.fiapvideomanagement.domain.model.VideoMessage;
import java.time.LocalDateTime;
import java.util.UUID;

public class Mapper {

    public static Video toVideo(VideoEntity videoEntity) {
        return Video.builder()
                .id(videoEntity.getId())
                .name(videoEntity.getName())
                .url(videoEntity.getUrl())
                .customerId(videoEntity.getCustomerId())
                .customerEmail(videoEntity.getCustomerEmail())
                .status(videoEntity.getStatus())
                .createdAt(videoEntity.getCreatedAt())
                .updatedAt(videoEntity.getUpdatedAt())
                .build();
    }

    public static Video toVideo(VideoMessage videoMessage) {
        return Video.builder()
                .id(UUID.fromString(videoMessage.getVideoId()))
                .name(videoMessage.getOriginalName())
                .url(videoMessage.getS3Key())
                .customerId(videoMessage.getCustomerId())
                .customerEmail(videoMessage.getCustomerEmail())
                .build();
    }

    public static VideoEntity toVideoEntity(Video video) {
        return VideoEntity.builder()
                .id(video.getId())
                .name(video.getName())
                .url(video.getUrl())
                .customerId(video.getCustomerId())
                .customerEmail(video.getCustomerEmail())
                .status(video.getStatus())
                .createdAt(video.getCreatedAt() == null ? video.getCreatedAt() : java.time.LocalDateTime.now())
                .updatedAt(video.getUpdatedAt())
                .build();
    }

    public static VideoEntity toVideoEntity(VideoMessage video) {
        return VideoEntity.builder()
                .id(UUID.fromString(video.getVideoId()))
                .name(video.getOriginalName())
                .url(video.getS3Key())
                .customerId(video.getCustomerId())
                .customerEmail(video.getCustomerEmail())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
