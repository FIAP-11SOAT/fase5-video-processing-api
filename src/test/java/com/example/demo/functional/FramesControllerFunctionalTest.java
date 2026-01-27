package com.example.demo.functional;

import com.example.demo.adapters.inbound.http.FramesController;
import com.example.demo.core.model.S3File;
import com.example.demo.core.ports.FramesDownloadServicePort;
import com.example.demo.shared.exceptions.ErrorType;
import com.example.demo.shared.exceptions.ExceptionUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


@WebMvcTest(FramesController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class FramesControllerFunctionalTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FramesDownloadServicePort framesService;

    @Value("${aws.s3.bucket.frames}")
    private String bucketName;

    @Test
    void shouldDownloadZipSuccessfully() throws Exception {
        byte[] content = "fake-zip-content".getBytes();

        S3File s3File = new S3File();
        s3File.setFileName("video-frames.zip");
        s3File.setContentLength(content.length);
        s3File.setInputStream(new ByteArrayInputStream(content));

        when(framesService.downloadZip("videoKey.zip", bucketName))
                .thenReturn(s3File);

        mockMvc.perform(get("/videos/download")
                        .param("videoKey", "videoKey.zip"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"video-frames.zip\""
                ))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().bytes(content));
    }

    @Test
    void shouldReturnBadRequestWhenZipNotFound() throws Exception {
        when(framesService.downloadZip("invalid.zip", bucketName))
                .thenThrow(ExceptionUtils.badRequest(
                        ErrorType.FILE_NOT_FOUND,
                        new RuntimeException("not found")
                ));

        mockMvc.perform(get("/videos/download")
                        .param("videoKey", "invalid.zip"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGenerateDownloadUrlSuccessfully() throws Exception {
        String url = "https://s3.amazonaws.com/bucket/videoKey.zip";

        when(framesService.getUrl("videoKey.zip", bucketName))
                .thenReturn(url);

        mockMvc.perform(get("/videos/download-url")
                        .param("videoKey", "videoKey.zip"))
                .andExpect(status().isOk())
                .andExpect(content().string(url));
    }

    @Test
    void shouldReturnBadRequestWhenGeneratingUrlForInvalidKey() throws Exception {
        when(framesService.getUrl("invalid.zip", bucketName))
                .thenThrow(ExceptionUtils.badRequest(
                        ErrorType.FILE_NOT_FOUND,
                        new RuntimeException("not found")
                ));

        mockMvc.perform(get("/videos/download-url")
                        .param("videoKey", "invalid.zip"))
                .andExpect(status().isBadRequest());
    }
}