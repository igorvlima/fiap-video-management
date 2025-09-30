package com.example.fiapvideomanagement.adapters.outbound.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringVideoRepository extends JpaRepository<VideoEntity, UUID> {
    List<VideoEntity> findByCustomerEmail(String customerEmail);
}
