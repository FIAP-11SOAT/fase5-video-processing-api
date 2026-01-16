package com.example.demo.adapters.inbound.http;

import com.example.demo.adapters.converter.VideoControllerConverter;
import com.example.demo.adapters.dto.http.VideoPostingBodyDto;
import com.example.demo.core.model.VideoPostingRequest;
import com.example.demo.core.ports.VideoPostingServicePort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/videos")
public class VideoController {

    private final VideoPostingServicePort videoPostingService;
    private final VideoControllerConverter converter;

    public VideoController(VideoPostingServicePort videoPostingService, VideoControllerConverter converter) {
        this.videoPostingService = videoPostingService;
        this.converter = converter;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadVideo(
            @RequestPart("file") MultipartFile file,
            @RequestPart VideoPostingBodyDto body
            ) throws IOException {

        VideoPostingRequest request = converter.convertToVideoPostingRequest(body, "123");
        videoPostingService.upload(file, request);
        return ResponseEntity.accepted().build();
    }
}
