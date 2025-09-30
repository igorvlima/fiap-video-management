package com.example.fiapvideomanagement.adapters.outbound.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "video")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoEntity {
    @Id
    private UUID id;
    private String name;
    private String url;
    private Long customerId;
    private String customerEmail;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
