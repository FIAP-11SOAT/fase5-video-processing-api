package com.example.demo.adapters.outbound.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@Profile("dev")
public class MongoRepository implements RepositoryPort {

    private final MongoTemplate mongoTemplate;

    public MongoRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
}
