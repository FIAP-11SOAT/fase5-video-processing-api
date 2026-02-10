package com.example.demo.adapters.outbound.repository;

import com.example.demo.adapters.converter.VideoModelMapper;
import com.example.demo.adapters.outbound.model.VideoDynamoModel;
import com.example.demo.core.model.Video;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Repository
@Profile({"prod", "dev"})
public class DynamoRepository implements RepositoryPort {

    private final DynamoDbTable<VideoDynamoModel> table;

    public DynamoRepository(
            DynamoDbEnhancedClient enhancedClient,
            @Value("${aws.dynamo.table.videos}") String tableName
    ) {
        this.table = enhancedClient.table(
                tableName,
                TableSchema.fromBean(VideoDynamoModel.class)
        );
    }

    @Override
    public void save(Video video) {
        try {
            VideoDynamoModel videoDynamoModel = VideoModelMapper.toDynamo(video);
            table.putItem(videoDynamoModel);
            log.info("[DynamoRepository] Vídeo salvo no DynamoDB com id={}", video.getId());
        } catch (Exception e){
            log.error(
                    "Error saving file",
                    kv("class", "DynamoRepository"),
                    kv("videoKey", video.getVideoKey()),
                    kv("userId", video.getUserId()),
                    e
            );
            throw e;
        }
    }

    @Override
    public List<Video> findByUserId(String userId) {

        DynamoDbIndex<VideoDynamoModel> index =
                table.index("userId-index");

        QueryConditional query = QueryConditional
                .keyEqualTo(Key.builder()
                        .partitionValue(userId)
                        .build());

        return index.query(query)
                .stream()
                .flatMap(page -> page.items().stream().map(VideoModelMapper::toDomain))
                .toList();

    }

    @Override
    public Optional<Video> findByVideoKey(String videoKey) {
        log.info("[DynamoRepository] Buscando vídeo por videoKey={}", videoKey);
        
        try {
            // Usando scan com filtro pois não há GSI para videoKey
            ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder()
                    .filterExpression(
                            Expression.builder()
                                    .expression("videoKey = :videoKey")
                                    .expressionValues(Map.of(
                                            ":videoKey", AttributeValue.builder().s(videoKey).build()
                                    ))
                                    .build()
                    )
                    .build();
            
            VideoDynamoModel item = table.scan(scanRequest)
                    .items()
                    .stream()
                    .findFirst()
                    .orElse(null);
            
            log.info("[DynamoRepository] Resultado da busca: {}", item != null ? "encontrado" : "não encontrado");
            return Optional.ofNullable(item)
                    .map(VideoModelMapper::toDomain);
        } catch (Exception e) {
            log.error("[DynamoRepository] Erro ao buscar videoKey={}: {}", videoKey, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Optional<Video> findById(String id) {
        try {
            ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder()
                    .filterExpression(
                            Expression.builder()
                                    .expression("id = :id")
                                    .expressionValues(Map.of(
                                            ":id", AttributeValue.builder().s(id).build()
                                    ))
                                    .build()
                    )
                    .build();
            
            VideoDynamoModel item = table.scan(scanRequest)
                    .items()
                    .stream()
                    .findFirst()
                    .orElse(null);
            
            return Optional.ofNullable(item).map(VideoModelMapper::toDomain);
        } catch (Exception e) {
            log.error(
                    "Error finding video on repository",
                    kv("class", "DynamoRepository"),
                    kv("videoId", id),
                    e
            );
            throw e;
        }
    }
}
