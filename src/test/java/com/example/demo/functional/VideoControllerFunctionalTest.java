package com.example.demo.functional;

import com.example.demo.adapters.converter.VideoControllerConverter;
import com.example.demo.adapters.dto.VideoResponseDto;
import com.example.demo.adapters.inbound.http.VideoController;
import com.example.demo.core.model.VideoPostingRequest;
import com.example.demo.core.ports.VideoPostingServicePort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


@WebMvcTest(VideoController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
public class VideoControllerFunctionalTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VideoPostingServicePort videoPostingService;

    @MockBean
    private VideoControllerConverter converter;

    @Value("${aws.s3.bucket.videos}")
    private String bucketName;

    @Test
    void shouldUploadVideoSuccessfully() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "video.mp4",
                "video/mp4",
                "fake-video-content".getBytes()
        );

        MockMultipartFile fileName = new MockMultipartFile(
                "file_name",
                "",
                "text/plain",
                "meu-video".getBytes()
        );

        VideoPostingRequest request =
                new VideoPostingRequest("meu-video", "123", "user", file);

        when(converter.convertToVideoPostingRequest("meu-video", "123", "user", file))
                .thenReturn(request);

        doNothing().when(videoPostingService)
                .upload(any(VideoPostingRequest.class), eq(bucketName));

        mockMvc.perform(multipart("/videos")
                        .file(file)
                        .file(fileName))
                .andExpect(status().isAccepted());

        verify(videoPostingService)
                .upload(any(VideoPostingRequest.class), eq(bucketName));
    }

    @Test
    void shouldReturnBadRequestWhenFileIsMissing() throws Exception {
        MockMultipartFile fileName = new MockMultipartFile(
                "file_name",
                "",
                "text/plain",
                "meu-video".getBytes()
        );

        mockMvc.perform(multipart("/videos")
                        .file(fileName))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(videoPostingService);
    }

    @Test
    void shouldReturnInternalServerErrorWhenServiceThrowsException() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "video.mp4",
                "video/mp4",
                "fake-video-content".getBytes()
        );

        MockMultipartFile fileName = new MockMultipartFile(
                "file_name",
                "",
                "text/plain",
                "meu-video".getBytes()
        );

        VideoPostingRequest request =
                new VideoPostingRequest("meu-video", "123", "user", file);

        when(converter.convertToVideoPostingRequest("meu-video", "123", "user", file))
                .thenReturn(request);

        doThrow(new RuntimeException("erro"))
                .when(videoPostingService)
                .upload(any(), any());

        mockMvc.perform(multipart("/videos")
                        .file(file)
                        .file(fileName))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturnVideosList() throws Exception {
        VideoResponseDto video = new VideoResponseDto(
                "123/abc",
                UUID.randomUUID(),
                "123",
                "meu-video",
                "uploaded",
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(videoPostingService.getVideos("123"))
                .thenReturn(List.of(video));

        mockMvc.perform(get("/videos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("meu-video"))
                .andExpect(jsonPath("$[0].status").value("uploaded"));
    }

    @Test
    void shouldReturnEmptyListWhenNoVideosFound() throws Exception {
        when(videoPostingService.getVideos("123"))
                .thenReturn(List.of());

        mockMvc.perform(get("/videos"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
