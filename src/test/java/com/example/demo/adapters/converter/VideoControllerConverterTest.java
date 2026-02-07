package com.example.demo.adapters.converter;

import com.example.demo.core.model.VideoPostingRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class VideoControllerConverterTest {

    private final VideoControllerConverter converter =
            new VideoControllerConverter();

    @Test
    void shouldConvertToVideoPostingRequestCorrectly() {
        // given
        String fileName = "video.mp4";
        String userId = "user-123";
        String userName = "user";

        MultipartFile file = new MockMultipartFile(
                "file",
                "video.mp4",
                "video/mp4",
                "fake-content".getBytes()
        );

        // when
        VideoPostingRequest result =
                converter.convertToVideoPostingRequest(userId, userName, file);

        // then
        assertNotNull(result);
        assertEquals(fileName, result.file().getOriginalFilename());
        assertEquals(userId, result.userId());
        assertEquals(file, result.file());
    }

}