package com.example.demo.adapters.outbound.repository;

import com.example.demo.adapters.converter.VideoModelMapper;
import com.example.demo.adapters.outbound.model.VideoDynamoModel;
import com.example.demo.core.model.Video;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@Profile({"prod", "dev"})
public class DynamoRepository implements RepositoryPort {

    private final DynamoDbTable<VideoDynamoModel> table;

    public DynamoRepository(DynamoDbEnhancedClient enhancedClient) {
        this.table = enhancedClient.table(
                "videos",
                TableSchema.fromBean(VideoDynamoModel.class)
        );
    }

    @Override
    public void save(Video video) {
        VideoDynamoModel videoDynamoModel = VideoModelMapper.toDynamo(video);
        table.putItem(videoDynamoModel);
        log.info("[DynamoRepository] Vídeo salvo no DynamoDB com id={}", video.getId());
    }

    @Override
    public Optional<Video> findById(UUID id) {
        return Optional.empty();
    }

    @Override
    public List<Video> findByUserId(String userId) {
        return List.of();
    }
}
