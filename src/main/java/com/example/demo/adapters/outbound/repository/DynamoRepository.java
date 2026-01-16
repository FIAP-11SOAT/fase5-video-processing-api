package com.example.demo.adapters.outbound.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Slf4j
@Repository
@Profile("prod")
public class DynamoRepository implements RepositoryPort {

    private final DynamoDbClient dynamoDbClient;
    private final ObjectMapper objectMapper;

    public DynamoRepository(DynamoDbClient dynamoDbClient, ObjectMapper objectMapper) {
        this.dynamoDbClient = dynamoDbClient;
        this.objectMapper = objectMapper;
    }

}
