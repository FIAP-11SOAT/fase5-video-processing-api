package com.example.demo.adapters.outbound.repository;

import com.example.demo.core.model.Video;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Getter
@Setter
@Repository
@Profile("test")
public class MockRepository implements RepositoryPort{

    @Override
    public void save(Video video) {

    }
}
