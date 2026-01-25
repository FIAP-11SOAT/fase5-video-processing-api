package com.example.demo.adapters.outbound.repository;

import com.example.demo.core.model.Video;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MockRepositoryTest {

    private MockRepository repository;

    @BeforeEach
    void setup() {
        repository = new MockRepository();
        repository.clear();
    }

    @Test
    void shouldSaveAndFindByVideoKey() {
        // arrange
        Video video = buildVideo(
                "key-1",
                "user-1",
                "video.mp4"
        );

        // act
        repository.save(video);
        Optional<Video> result = repository.findByVideoKey("key-1");

        // assert
        assertTrue(result.isPresent());
        assertEquals("key-1", result.get().getVideoKey());
        assertEquals("user-1", result.get().getUserId());
        assertEquals("video.mp4", result.get().getName());
    }

    @Test
    void shouldReturnEmptyOptionalWhenVideoKeyNotFound() {
        // act
        Optional<Video> result = repository.findByVideoKey("not-found");

        // assert
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindVideosByUserId() {
        // arrange
        Video video1 = buildVideo("key-1", "user-1", "a.mp4");
        Video video2 = buildVideo("key-2", "user-1", "b.mp4");
        Video video3 = buildVideo("key-3", "user-2", "c.mp4");

        repository.save(video1);
        repository.save(video2);
        repository.save(video3);

        // act
        List<Video> result = repository.findByUserId("user-1");

        // assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(v -> "user-1".equals(v.getUserId())));
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoVideos() {
        // arrange
        Video video = buildVideo("key-1", "user-1", "video.mp4");
        repository.save(video);

        // act
        List<Video> result = repository.findByUserId("user-x");

        // assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldOverwriteVideoWhenSavingSameVideoKey() {
        // arrange
        Video video1 = buildVideo("key-1", "user-1", "old.mp4");
        Video video2 = buildVideo("key-1", "user-1", "new.mp4");

        // act
        repository.save(video1);
        repository.save(video2);

        Optional<Video> result = repository.findByVideoKey("key-1");

        // assert
        assertTrue(result.isPresent());
        assertEquals("new.mp4", result.get().getName());
    }

    @Test
    void shouldNotExposeInternalStateOnSave() {
        // arrange
        Video original = buildVideo("key-1", "user-1", "video.mp4");

        repository.save(original);

        // mutate original object
        original.setName("changed.mp4");

        // act
        Video stored = repository.findByVideoKey("key-1").orElseThrow();

        // assert
        assertEquals("video.mp4", stored.getName());
    }

    @Test
    void shouldNotExposeInternalStateOnFind() {
        // arrange
        Video video = buildVideo("key-1", "user-1", "video.mp4");
        repository.save(video);

        Video retrieved = repository.findByVideoKey("key-1").orElseThrow();

        // mutate retrieved object
        retrieved.setName("changed.mp4");

        // act
        Video again = repository.findByVideoKey("key-1").orElseThrow();

        // assert
        assertEquals("video.mp4", again.getName());
    }

    @Test
    void shouldClearRepository() {
        // arrange
        repository.save(buildVideo("key-1", "user-1", "a.mp4"));
        repository.save(buildVideo("key-2", "user-2", "b.mp4"));

        // act
        repository.clear();

        // assert
        assertTrue(repository.findByUserId("user-1").isEmpty());
        assertTrue(repository.findByVideoKey("key-1").isEmpty());
    }

    private Video buildVideo(String key, String userId, String name) {
        Video v = new Video();
        v.setId(UUID.randomUUID());
        v.setVideoKey(key);
        v.setUserId(userId);
        v.setName(name);
        v.setStatus("uploaded");
        v.setCreatedAt(OffsetDateTime.now());
        v.setUpdatedAt(OffsetDateTime.now());
        return v;
    }
}
