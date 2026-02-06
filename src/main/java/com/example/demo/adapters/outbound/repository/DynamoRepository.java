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

import java.util.List;
import java.util.Optional;

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
        VideoDynamoModel videoDynamoModel = VideoModelMapper.toDynamo(video);
        table.putItem(videoDynamoModel);
        log.info("[DynamoRepository] Vídeo salvo no DynamoDB com id={}", video.getId());
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

        VideoDynamoModel item = table.getItem(r ->
                r.key(k -> k.partitionValue(videoKey))
        );

        return Optional.ofNullable(item)
                .map(VideoModelMapper::toDomain);
    }
}
