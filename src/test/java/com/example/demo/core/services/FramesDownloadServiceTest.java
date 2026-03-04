package com.example.demo.core.services;

import com.example.demo.adapters.outbound.repository.RepositoryPort;
import com.example.demo.adapters.outbound.storage.FileStoragePort;
import com.example.demo.core.model.S3File;
import com.example.demo.core.model.Video;
import com.example.demo.shared.exceptions.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

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

    @Test
    void shouldDownloadZipWhenVideoExists() {
        // arrange
        UUID videoId = UUID.randomUUID();
        String userId = "123";
        String videoKey = "user" + "/" + videoId.toString() + ".zip";
        String bucketName = "bucket-test";

        Video video = new Video();
        video.setId(videoId);
        video.setName("meu-video");
        video.setVideoKey("user/" + videoId);
        video.setUserId(userId);

        S3File s3File = new S3File();

        when(repository.findById(videoId.toString()))
                .thenReturn(Optional.of(video));

        when(fileStorage.downloadAsStream(videoKey, bucketName))
                .thenReturn(s3File);

        // act
        S3File result = service.downloadZip(videoId.toString(), bucketName);

        // assert
        assertNotNull(result);
        assertEquals("meu-video", result.getFileName());

        verify(fileStorage, times(1))
                .downloadAsStream(videoKey, bucketName);
    }

    @Test
    void shouldThrowExceptionWhenVideoNotFoundOnDownload() {
        // arrange
        when(repository.findById("not-found"))
                .thenReturn(Optional.empty());

        // act + assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.downloadZip("not-found", "bucket")
        );

        assertEquals(
                ErrorType.FILE_NOT_FOUND.getMessage(),
                exception.getMessage()
        );

        verify(fileStorage, never()).downloadAsStream(any(), any());
    }

    @Test
    void shouldReturnPresignedUrlWhenUserIsOwner() {
        // arrange
        UUID videoId = UUID.randomUUID();
        String userId = "123";
        String videoKey = "user" + "/" + videoId.toString() + ".zip";
        String bucketName = "bucket-test";


        Video video = new Video();
        video.setId(videoId);
        video.setName("meu-video");
        video.setVideoKey("user/" + videoId);
        video.setUserId(userId);

        when(repository.findById(videoId.toString()))
                .thenReturn(Optional.of(video));

        when(fileStorage.generatePresignedUrl(videoKey, bucketName))
                .thenReturn("https://signed-url");

        // act
        String url = service.getUrl(videoId.toString(), bucketName, userId);

        // assert
        assertEquals("https://signed-url", url);

        verify(fileStorage, times(1))
                .generatePresignedUrl(videoKey, bucketName);
    }

    @Test
    void shouldThrowFileNotFoundWhenVideoDoesNotExistOnGetUrl() {
        // arrange
        when(repository.findById("video-x"))
                .thenReturn(Optional.empty());

        // act + assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.getUrl("video-x", "bucket", "user")
        );

        assertEquals(
                ErrorType.FILE_NOT_FOUND.getMessage(),
                exception.getMessage()
        );

        verify(fileStorage, never()).generatePresignedUrl(any(), any());
    }

    @Test
    void shouldThrowForbiddenWhenUserIsNotOwner() {
        // arrange
        Video video = new Video();
        UUID videoId = UUID.randomUUID();
        video.setId(videoId);
        video.setUserId("123");
        video.setVideoKey("123/" + videoId);

        when(repository.findById(videoId.toString()))
                .thenReturn(Optional.of(video));

        // act + assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.getUrl(videoId.toString(), "bucket", "456")
        );

        assertEquals(
                ErrorType.FORBIDDEN.getMessage(),
                exception.getMessage()
        );

        verify(fileStorage, never()).generatePresignedUrl(any(), any());
    }
}
