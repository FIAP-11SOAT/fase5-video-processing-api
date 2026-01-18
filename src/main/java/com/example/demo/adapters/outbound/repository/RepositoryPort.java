package com.example.demo.adapters.outbound.repository;

import com.example.demo.core.model.Video;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepositoryPort {
    void save(Video video);
    Optional<Video> findById(UUID id);
    List<Video> findByUserId(String userId);
}
