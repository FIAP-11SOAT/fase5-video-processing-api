package com.example.demo.adapters.outbound.storage;

import com.example.demo.core.model.S3File;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3FileStorageAdapterTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private S3FileStorageAdapter adapter;

    private static final String BUCKET = "videos-bucket";
    private static final String VIDEO_KEY = "user-1/video-uuid.mp4";

    @Test
    void shouldDownloadFileAsStreamSuccessfully() {
        // arrange
        GetObjectResponse response = GetObjectResponse.builder()
                .contentLength(123L)
                .build();

        InputStream inputStream = new ByteArrayInputStream(new byte[0]);

        AbortableInputStream abortableInputStream =
                AbortableInputStream.create(inputStream);

        ResponseInputStream<GetObjectResponse> responseInputStream =
                new ResponseInputStream<>(response, abortableInputStream);

        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenReturn(responseInputStream);

        // act
        S3File result = adapter.downloadAsStream(VIDEO_KEY, BUCKET);

        // assert
        assertNotNull(result);
        assertEquals(123L, result.getContentLength());
        assertNotNull(result.getInputStream());

        ArgumentCaptor<GetObjectRequest> captor =
                ArgumentCaptor.forClass(GetObjectRequest.class);

        verify(s3Client).getObject(captor.capture());

        GetObjectRequest request = captor.getValue();
        assertEquals(BUCKET, request.bucket());
        assertEquals(VIDEO_KEY, request.key());
    }

    @Test
    void shouldThrowRuntimeExceptionWhenFileNotFound() {
        // arrange
        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().build());

        // act + assert
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> adapter.downloadAsStream(VIDEO_KEY, BUCKET)
        );

        assertTrue(ex.getMessage().contains("Arquivo não encontrado"));
    }

    @Test
    void shouldUploadFileSuccessfully() throws IOException {
        // arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "video.mp4",
                "video/mp4",
                "video-content".getBytes()
        );

        // act
        adapter.uploadFile(file, VIDEO_KEY, BUCKET);

        // assert
        ArgumentCaptor<PutObjectRequest> requestCaptor =
                ArgumentCaptor.forClass(PutObjectRequest.class);

        verify(s3Client).putObject(
                requestCaptor.capture(),
                any(RequestBody.class)
        );

        PutObjectRequest request = requestCaptor.getValue();
        assertEquals(BUCKET, request.bucket());
        assertEquals(VIDEO_KEY, request.key());
        assertEquals("video/mp4", request.contentType());
    }

    @Test
    void shouldRethrowExceptionWhenUploadFails() throws IOException {
        // arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "video.mp4",
                "video/mp4",
                "video-content".getBytes()
        );

        doThrow(new RuntimeException("S3 error"))
                .when(s3Client)
                .putObject(any(PutObjectRequest.class), any(RequestBody.class));

        // act + assert
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> adapter.uploadFile(file, VIDEO_KEY, BUCKET)
        );

        assertEquals("S3 error", ex.getMessage());
    }

    @Test
    void shouldGeneratePresignedUrlSuccessfully() throws MalformedURLException {
        // arrange
        PresignedGetObjectRequest presignedRequest =
                mock(PresignedGetObjectRequest.class);

        when(presignedRequest.url())
                .thenReturn(URI.create("https://presigned-url").toURL());

        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .thenReturn(presignedRequest);

        // act
        String url = adapter.generatePresignedUrl(VIDEO_KEY, BUCKET);

        // assert
        assertEquals("https://presigned-url", url);

        ArgumentCaptor<GetObjectPresignRequest> captor =
                ArgumentCaptor.forClass(GetObjectPresignRequest.class);

        verify(s3Presigner).presignGetObject(captor.capture());

        GetObjectPresignRequest request = captor.getValue();
        assertEquals(Duration.ofMinutes(10), request.signatureDuration());
        assertEquals(BUCKET, request.getObjectRequest().bucket());
        assertEquals(VIDEO_KEY, request.getObjectRequest().key());
    }

    @Test
    void shouldRethrowExceptionWhenPresignFails() {
        // arrange
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .thenThrow(new RuntimeException("presign error"));

        // act + assert
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> adapter.generatePresignedUrl(VIDEO_KEY, BUCKET)
        );

        assertEquals("presign error", ex.getMessage());
    }
}
