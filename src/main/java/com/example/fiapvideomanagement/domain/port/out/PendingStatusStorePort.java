package com.example.fiapvideomanagement.domain.port.out;

import com.example.fiapvideomanagement.domain.model.VideoStatusMessage;

import java.util.Optional;

public interface PendingStatusStorePort {
    void savePendingStatus(String videoId, VideoStatusMessage message);
    Optional<VideoStatusMessage> getAndRemovePendingStatus(String videoId);
}