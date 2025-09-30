package com.example.fiapvideomanagement.adapters.inbound.rest;


import com.example.fiapvideomanagement.domain.model.Video;
import com.example.fiapvideomanagement.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @GetMapping
    public ResponseEntity<List<Video>> findByCustomerEmail(@RequestParam String customerEmail) {
        var list = videoService.findByCustomerEmail(customerEmail);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Video> getById(@PathVariable UUID id) {
        return videoService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody Video video) {
        videoService.createVideo(video);
        return ResponseEntity.ok().build();
    }
}
