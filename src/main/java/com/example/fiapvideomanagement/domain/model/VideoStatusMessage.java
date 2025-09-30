package com.example.fiapvideomanagement.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class VideoStatusMessage {
    private UUID videoId;
    private String videoName;
    private String customerEmail;
    private String videoStatus;
}
