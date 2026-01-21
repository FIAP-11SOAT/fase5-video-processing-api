package com.example.demo.adapters.outbound.repository;

import com.example.demo.core.model.Video;

import java.util.List;

public interface RepositoryPort {
    void save(Video video);
    List<Video> findByUserId(String userId);
}
