package com.example.demo.adapters.outbound.repository;

import com.example.demo.core.model.Video;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@Repository
@NoArgsConstructor
@Profile("test")
public class MockRepository implements RepositoryPort {

    private final Map<String, Video> table = new ConcurrentHashMap<>();

    @Override
    public void save(Video video) {
        table.put(video.getVideoKey(), clone(video));
    }

    @Override
    public List<Video> findByUserId(String userId) {
        return table.values().stream()
                .filter(video -> userId.equals(video.getUserId()))
                .map(this::clone)
                .toList();
    }

    @Override
    public Optional<Video> findByVideoKey(String videoKey) {
        return Optional.ofNullable(table.get(videoKey))
                .map(this::clone);
    }

    @Override
    public Optional<Video> findById(String id) {
        System.out.println("Size: " + this.table.size());
        return table.values().stream()
                .filter(video -> id.equals(video.getId().toString()))
                .findFirst()
                .map(this::clone);
    }

    private Video clone(Video source) {
        Video v = new Video();
        v.setId(source.getId());
        v.setVideoKey(source.getVideoKey());
        v.setName(source.getName());
        v.setUserId(source.getUserId());
        v.setStatus(source.getStatus());
        v.setCreatedAt(source.getCreatedAt());
        v.setUpdatedAt(source.getUpdatedAt());
        return v;
    }

    public void clear() {
        table.clear();
    }
}

