package com.example.fiapvideomanagement.domain.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class Video implements Serializable {
    private UUID id;
    private String name;
    private String url;
    private Long customerId;
    private String customerEmail;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
