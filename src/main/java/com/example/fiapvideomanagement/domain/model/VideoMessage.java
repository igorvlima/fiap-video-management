package com.example.fiapvideomanagement.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class VideoMessage {
    private String videoId;
    private String s3Key;
    private String originalName;
    private String generatedName;
    private Long customerId;
    private String customerEmail;
}
