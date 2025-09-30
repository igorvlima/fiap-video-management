package com.example.fiapvideomanagement.domain.port.out;


import com.example.fiapvideomanagement.domain.model.Video;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VideoRepositoryPort {
    Video save(Video video);
    Optional<Video> findById(UUID id);
    List<Video> findByCustomerEmail(String customerEmail);
    boolean existsById(UUID id);
}
