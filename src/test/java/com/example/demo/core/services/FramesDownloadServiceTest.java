package com.example.demo.core.services;

import com.example.demo.adapters.outbound.repository.RepositoryPort;
import com.example.demo.adapters.outbound.storage.FileStoragePort;
import com.example.demo.core.model.S3File;
import com.example.demo.core.model.Video;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FramesDownloadServiceTest {

    @Mock
    private FileStoragePort fileStorage;

    @Mock
    private RepositoryPort repository;

    @InjectMocks
    private FramesDownloadService service;

    private static final String BUCKET = "frames-bucket";
    private static final String VIDEO_KEY = "user-123/video-uuid.zip";
    private static final String VIDEO_KEY_WITHOUT_ZIP = "user-123/video-uuid";

    @Test
    void shouldDownloadZipSuccessfully() {
        // arrange
        Video video = new Video();
        video.setName("video.mp4");

        S3File s3File = new S3File();
        s3File.setInputStream(new ByteArrayInputStream("zip-content".getBytes()));

        when(repository.findByVideoKey(VIDEO_KEY_WITHOUT_ZIP))
                .thenReturn(Optional.of(video));

        when(fileStorage.downloadAsStream(BUCKET, VIDEO_KEY))
                .thenReturn(s3File);

        // act
        S3File result = service.downloadZip(VIDEO_KEY, BUCKET);

        // assert
        assertNotNull(result);
        assertEquals("video.mp4", result.getFileName());

        verify(repository, times(1))
                .findByVideoKey(VIDEO_KEY_WITHOUT_ZIP);

        verify(fileStorage, times(1))
                .downloadAsStream(BUCKET, VIDEO_KEY);
    }

    @Test
    void shouldThrowExceptionWhenVideoNotFoundOnDownload() {
        // arrange
        when(repository.findByVideoKey(VIDEO_KEY_WITHOUT_ZIP))
                .thenReturn(Optional.empty());

        // act + assert
        assertThrows(RuntimeException.class,
                () -> service.downloadZip(VIDEO_KEY, BUCKET));

        verify(fileStorage, never()).downloadAsStream(any(), any());
    }

    @Test
    void shouldReturnPresignedUrlSuccessfully() {
        // arrange
        String presignedUrl = "https://s3.aws.com/presigned-url";

        Video video = new Video();

        when(repository.findByVideoKey(VIDEO_KEY_WITHOUT_ZIP))
                .thenReturn(Optional.of(video));

        when(fileStorage.generatePresignedUrl(VIDEO_KEY, BUCKET))
                .thenReturn(presignedUrl);

        // act
        String result = service.getUrl(VIDEO_KEY, BUCKET);

        // assert
        assertEquals(presignedUrl, result);

        verify(repository, times(1))
                .findByVideoKey(VIDEO_KEY_WITHOUT_ZIP);

        verify(fileStorage, times(1))
                .generatePresignedUrl(VIDEO_KEY, BUCKET);
    }

    @Test
    void shouldThrowExceptionWhenVideoNotFoundOnGetUrl() {
        // arrange
        when(repository.findByVideoKey(VIDEO_KEY_WITHOUT_ZIP))
                .thenReturn(Optional.empty());

        // act + assert
        assertThrows(RuntimeException.class,
                () -> service.getUrl(VIDEO_KEY, BUCKET));

        verify(fileStorage, never())
                .generatePresignedUrl(any(), any());
    }
}
