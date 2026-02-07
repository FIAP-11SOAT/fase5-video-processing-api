package com.example.demo.adapters.outbound.repository;

import com.example.demo.adapters.converter.VideoModelMapper;
import com.example.demo.adapters.outbound.model.VideoDynamoModel;
import com.example.demo.core.model.Video;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;


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

        repository = new DynamoRepository(enhancedClient, "fase5-video-processing");
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

        SdkIterable<VideoDynamoModel> items =
                () -> List.of(dynamoModel).iterator();

        PageIterable<VideoDynamoModel> pageIterable =
                mock(PageIterable.class);

        when(pageIterable.items()).thenReturn(items);
        when(table.scan(any(ScanEnhancedRequest.class)))
                .thenReturn(pageIterable);

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
        SdkIterable<VideoDynamoModel> emptyItems =
                () -> List.<VideoDynamoModel>of().iterator();

        PageIterable<VideoDynamoModel> pageIterable =
                mock(PageIterable.class);

        when(pageIterable.items()).thenReturn(emptyItems);
        when(table.scan(any(ScanEnhancedRequest.class)))
                .thenReturn(pageIterable);

        // act
        Optional<Video> result = repository.findByVideoKey("not-found");

        // assert
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindVideoById() {
        // arrange
        String id = UUID.randomUUID().toString();

        VideoDynamoModel dynamoModel = new VideoDynamoModel();
        Video domainVideo = new Video();

        SdkIterable<VideoDynamoModel> items =
                () -> List.of(dynamoModel).iterator();

        PageIterable<VideoDynamoModel> pageIterable =
                mock(PageIterable.class);

        when(pageIterable.items()).thenReturn(items);
        when(table.scan(any(ScanEnhancedRequest.class)))
                .thenReturn(pageIterable);

        try (MockedStatic<VideoModelMapper> mapper = mockStatic(VideoModelMapper.class)) {
            mapper.when(() -> VideoModelMapper.toDomain(dynamoModel))
                    .thenReturn(domainVideo);

            // act
            Optional<Video> result = repository.findById(id);

            // assert
            assertTrue(result.isPresent());
            assertEquals(domainVideo, result.get());
            verify(table, times(1)).scan(any(ScanEnhancedRequest.class));
        }
    }

    @Test
    void shouldThrowExceptionWhenScanFailsOnFindByVideoKey() {
        // arrange
        when(table.scan(any(ScanEnhancedRequest.class)))
                .thenThrow(new RuntimeException("Dynamo error"));

        // act + assert
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> repository.findByVideoKey("video-key")
        );

        assertEquals("Dynamo error", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenScanFailsOnFindById() {
        // arrange
        when(table.scan(any(ScanEnhancedRequest.class)))
                .thenThrow(new RuntimeException("Scan failed"));

        // act + assert
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> repository.findById("id-123")
        );

        assertEquals("Scan failed", ex.getMessage());
    }



}
