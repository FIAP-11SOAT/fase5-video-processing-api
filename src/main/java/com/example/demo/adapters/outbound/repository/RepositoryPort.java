package com.example.demo.adapters.outbound.repository;

import com.example.demo.core.model.Video;

import java.util.List;
import java.util.Optional;

public interface RepositoryPort {
    void save(Video video);
    List<Video> findByUserId(String userId);
    Optional<Video> findByVideoKey(String videoKey);
}
