package com.example.demo.core.services;

import com.example.demo.adapters.dto.VideoResponseDto;
import com.example.demo.adapters.outbound.repository.RepositoryPort;
import com.example.demo.adapters.outbound.storage.FileStoragePort;
import com.example.demo.core.model.Video;
import com.example.demo.core.model.VideoPostingRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoPostingServiceTest {

    @Mock
    private FileStoragePort fileStorage;

    @Mock
    private RepositoryPort repository;

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
                "video.mp4",
                file
        );

        // act
        service.upload(request, BUCKET);

        // assert
        verify(fileStorage, times(1))
                .uploadFile(any(MultipartFile.class), anyString(), eq(BUCKET));

        verify(repository, times(1))
                .save(any(Video.class));
    }

    @Test
    void shouldThrowExceptionWhenFileSizeExceedsLimit() throws IOException {
        // arrange
        byte[] bigFile = new byte[(int) (201L * 1024)];

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "big-video.mp4",
                "video/mp4",
                bigFile
        );

        VideoPostingRequest request = new VideoPostingRequest(
                USER_ID,
                "big-video.mp4",
                file
        );

        // act + assert
        assertThrows(RuntimeException.class,
                () -> service.upload(request, BUCKET));

        verify(fileStorage, never()).uploadFile(any(), anyString(), any());
        verify(repository, never()).save(any());
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
                "file.txt",
                file
        );

        // act + assert
        assertThrows(RuntimeException.class,
                () -> service.upload(request, BUCKET));

        verify(fileStorage, never()).uploadFile(any(), anyString(), any());
        verify(repository, never()).save(any());
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
                "video.mp4",
                file
        );

        doThrow(new IOException("S3 error"))
                .when(fileStorage)
                .uploadFile(any(), anyString(), anyString());

        // act + assert
        assertThrows(IOException.class,
                () -> service.upload(request, BUCKET));

        verify(repository, never()).save(any());
    }

    @Test
    void shouldReturnVideosByUserId() {
        // arrange
        Video video1 = new Video();
        video1.setUserId(USER_ID);

        Video video2 = new Video();
        video2.setUserId(USER_ID);

        when(repository.findByUserId(USER_ID))
                .thenReturn(List.of(video1, video2));

        // act
        List<VideoResponseDto> result = service.getVideos(USER_ID);

        // assert
        assertNotNull(result);
        assertEquals(2, result.size());

        verify(repository, times(1)).findByUserId(USER_ID);
    }
}
