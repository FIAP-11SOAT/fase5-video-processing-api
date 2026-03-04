package com.example.demo.core.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.adapters.converter.VideoModelMapper;
import com.example.demo.adapters.dto.VideoResponseDto;
import com.example.demo.adapters.outbound.model.NotificationPayload;
import com.example.demo.adapters.outbound.notification_queue.NotificationPort;
import com.example.demo.adapters.outbound.repository.RepositoryPort;
import com.example.demo.adapters.outbound.storage.FileStoragePort;
import com.example.demo.core.model.Video;
import com.example.demo.core.model.VideoPostingRequest;

@ExtendWith(MockitoExtension.class)
class VideoPostingServiceTest {

    @Mock
    private FileStoragePort fileStorage;

    @Mock
    private RepositoryPort repository;

    @Mock
    private NotificationPort notificationService;

    @InjectMocks
    private VideoPostingService service;

    private static final String BUCKET = "videos-bucket";
    private static final String USER_ID = "user-123";

    @Test
    void shouldUploadVideoSuccessfully() throws IOException {
        // arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "video.mp4",
                "video/mp4",
                "video-content".getBytes()
        );

        VideoPostingRequest request = new VideoPostingRequest(
                USER_ID,
                "user",
                file
        );

        // act
        service.upload(request, BUCKET);

        // assert
        verify(fileStorage, times(1))
                .uploadFile(any(MultipartFile.class), anyString(), eq(BUCKET));

        verify(repository, times(1))
                .save(any(Video.class));

        verify(notificationService, times(1))
                .send(any(NotificationPayload.class));
    }

    @Test
    void shouldSendCorrectNotificationPayload() throws IOException {
        // arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "video.mp4",
                "video/mp4",
                "video-content".getBytes()
        );

        VideoPostingRequest request = new VideoPostingRequest(
                USER_ID,
                "user",
                file
        );

        ArgumentCaptor<NotificationPayload> captor =
                ArgumentCaptor.forClass(NotificationPayload.class);

        // act
        service.upload(request, BUCKET);

        // assert
        verify(notificationService).send(captor.capture());

        NotificationPayload payload = captor.getValue();
        assertEquals(USER_ID, payload.userId());
        assertEquals("video.mp4", payload.videoName());
        assertEquals("uploaded", payload.status());
        assertTrue(payload.videoKey().startsWith("user/"));
    }

    @Test
    void shouldThrowExceptionWhenFileSizeExceedsLimit() throws IOException {
        // arrange
        byte[] bigFile = new byte[(int) (501L * 1024 * 1024)];

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "big-video.mp4",
                "video/mp4",
                bigFile
        );

        VideoPostingRequest request = new VideoPostingRequest(
                USER_ID,
                "user",
                file
        );

        // act + assert
        assertThrows(RuntimeException.class,
                () -> service.upload(request, BUCKET));

        verify(fileStorage, never()).uploadFile(any(), anyString(), any());
        verify(repository, never()).save(any());
        verify(notificationService, never()).send(any());
    }

    @Test
    void shouldThrowExceptionWhenFileIsNotVideo() throws IOException {
        // arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "file.txt",
                "text/plain",
                "text-content".getBytes()
        );

        VideoPostingRequest request = new VideoPostingRequest(
                USER_ID,
                "user",
                file
        );

        // act + assert
        assertThrows(RuntimeException.class,
                () -> service.upload(request, BUCKET));

        verify(fileStorage, never()).uploadFile(any(), anyString(), any());
        verify(repository, never()).save(any());
        verify(notificationService, never()).send(any());
    }

    @Test
    void shouldRethrowExceptionWhenStorageFails() throws IOException {
        // arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "video.mp4",
                "video/mp4",
                "video-content".getBytes()
        );

        VideoPostingRequest request = new VideoPostingRequest(
                USER_ID,
                "user",
                file
        );

        doThrow(new IOException("S3 error"))
                .when(fileStorage)
                .uploadFile(any(), anyString(), anyString());

        // act + assert
        assertThrows(IOException.class,
                () -> service.upload(request, BUCKET));

        verify(repository, never()).save(any());
        verify(notificationService, never()).send(any());
    }

    @Test
    void shouldReturnVideosByUserId() {
        // arrange
        Video video1 = new Video();
        Video video2 = new Video();

        VideoResponseDto dto1 = new VideoResponseDto(
                "key-1", UUID.randomUUID(), USER_ID,
                "a.mp4", "uploaded",
                OffsetDateTime.now(), OffsetDateTime.now()
        );

        VideoResponseDto dto2 = new VideoResponseDto(
                "key-2", UUID.randomUUID(), USER_ID,
                "b.mp4", "uploaded",
                OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(repository.findByUserId(USER_ID))
                .thenReturn(List.of(video1, video2));

        try (MockedStatic<VideoModelMapper> mapper =
                     mockStatic(VideoModelMapper.class)) {

            mapper.when(() -> VideoModelMapper.toDto(video1))
                    .thenReturn(dto1);

            mapper.when(() -> VideoModelMapper.toDto(video2))
                    .thenReturn(dto2);

            // act
            List<VideoResponseDto> result = service.getVideos(USER_ID);

            // assert
            assertEquals(2, result.size());
        }
    }

    @Test
    void shouldReturnVideoByVideoKey() {
        // arrange
        String videoKey = "user/video-id";

        Video video = new Video();

        VideoResponseDto dto = new VideoResponseDto(
                videoKey,
                UUID.randomUUID(),
                USER_ID,
                "video.mp4",
                "uploaded",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(repository.findByVideoKey(videoKey))
                .thenReturn(Optional.of(video));

        try (MockedStatic<VideoModelMapper> mapper =
                     mockStatic(VideoModelMapper.class)) {

            mapper.when(() -> VideoModelMapper.toDto(video))
                    .thenReturn(dto);

            // act
            VideoResponseDto result = service.getVideoByVideoKey(videoKey);

            // assert
            assertEquals(dto, result);
        }
    }

    @Test
    void shouldThrowExceptionWhenVideoKeyNotFound() {
        // arrange
        when(repository.findByVideoKey("not-found"))
                .thenReturn(Optional.empty());

        // act + assert
        assertThrows(RuntimeException.class,
                () -> service.getVideoByVideoKey("not-found"));
    }

    @Test
    void shouldNotSendNotificationWhenRepositorySaveFails() throws IOException {
        // arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "video.mp4",
                "video/mp4",
                "video-content".getBytes()
        );

        VideoPostingRequest request = new VideoPostingRequest(
                USER_ID,
                "user",
                file
        );

        doThrow(new RuntimeException("DB error"))
                .when(repository)
                .save(any(Video.class));

        // act + assert
        assertThrows(RuntimeException.class,
                () -> service.upload(request, BUCKET));

        verify(notificationService, never()).send(any());
    }
}

