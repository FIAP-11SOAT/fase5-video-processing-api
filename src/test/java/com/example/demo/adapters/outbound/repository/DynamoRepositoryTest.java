package com.example.demo.adapters.outbound.repository;

import com.example.demo.adapters.converter.VideoModelMapper;
import com.example.demo.adapters.outbound.model.VideoDynamoModel;
import com.example.demo.core.model.Video;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DynamoRepositoryTest {

    @Mock
    private DynamoDbEnhancedClient enhancedClient;

    @Mock
    private DynamoDbTable<VideoDynamoModel> table;

    @Mock
    private DynamoDbIndex<VideoDynamoModel> index;

    private DynamoRepository repository;

    @BeforeEach
    void setup() {
        when(enhancedClient.table(
                anyString(),
                any(TableSchema.class)
        )).thenReturn(table);

        repository = new DynamoRepository(enhancedClient);
    }

    @Test
    void shouldSaveVideoSuccessfully() {
        // arrange
        Video video = new Video();
        video.setId(UUID.randomUUID());

        VideoDynamoModel dynamoModel = new VideoDynamoModel();

        try (MockedStatic<VideoModelMapper> mapper = mockStatic(VideoModelMapper.class)) {
            mapper.when(() -> VideoModelMapper.toDynamo(video))
                    .thenReturn(dynamoModel);

            // act
            repository.save(video);

            // assert
            verify(table, times(1)).putItem(dynamoModel);
        }
    }

    @Test
    void shouldFindVideosByUserId() {
        // arrange
        String userId = "user-123";

        VideoDynamoModel dynamoModel = new VideoDynamoModel();
        Video domainVideo = new Video();

        Page<VideoDynamoModel> page =
                Page.create(List.of(dynamoModel));

        SdkIterable<Page<VideoDynamoModel>> iterable =
                () -> List.of(page).iterator();

        when(table.index("userId-index"))
                .thenReturn(index);

        when(index.query(any(QueryConditional.class)))
                .thenReturn(iterable);

        try (MockedStatic<VideoModelMapper> mapper = mockStatic(VideoModelMapper.class)) {
            mapper.when(() -> VideoModelMapper.toDomain(dynamoModel))
                    .thenReturn(domainVideo);

            // act
            List<Video> result = repository.findByUserId(userId);

            // assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(domainVideo, result.get(0));
        }
    }

    @Test
    void shouldReturnEmptyListWhenNoVideosFoundByUserId() {
        // arrange
        when(table.index("userId-index"))
                .thenReturn(index);

        when(index.query(any(QueryConditional.class)))
                .thenReturn(() -> List.<Page<VideoDynamoModel>>of().iterator());

        // act
        List<Video> result = repository.findByUserId("user-x");

        // assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindVideoByVideoKey() {
        // arrange
        String videoKey = "user-123/video-uuid";

        VideoDynamoModel dynamoModel = new VideoDynamoModel();
        Video domainVideo = new Video();

        when(table.getItem(any(Key.class)))
                .thenReturn(dynamoModel);

        try (MockedStatic<VideoModelMapper> mapper = mockStatic(VideoModelMapper.class)) {
            mapper.when(() -> VideoModelMapper.toDomain(dynamoModel))
                    .thenReturn(domainVideo);

            // act
            Optional<Video> result = repository.findByVideoKey(videoKey);

            // assert
            assertTrue(result.isPresent());
            assertEquals(domainVideo, result.get());
        }
    }

    @Test
    void shouldReturnEmptyOptionalWhenVideoKeyNotFound() {
        // arrange
        when(table.getItem(any(Key.class)))
                .thenReturn(null);

        // act
        Optional<Video> result = repository.findByVideoKey("not-found");

        // assert
        assertTrue(result.isEmpty());
    }
}
