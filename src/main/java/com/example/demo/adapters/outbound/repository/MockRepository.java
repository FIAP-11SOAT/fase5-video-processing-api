package com.example.demo.adapters.outbound.repository;

import com.example.demo.core.model.Video;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Getter
@Setter
@Repository
@Profile("test")
public class MockRepository implements RepositoryPort{

    @Override
    public void save(Video video) {

    }

    @Override
    public List<Video> findByUserId(String userId) {
        return List.of();
    }

    @Override
    public Optional<Video> findByVideoKey(String videoKey) {
        return Optional.empty();
    }
}
