package com.example.demo.adapters.inbound.http;

import com.example.demo.adapters.converter.VideoControllerConverter;
import com.example.demo.adapters.dto.VideoResponseDto;
import com.example.demo.core.model.VideoPostingRequest;
import com.example.demo.core.ports.VideoPostingServicePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/videos")
public class VideoController {

    private final VideoPostingServicePort videoPostingService;
    private final VideoControllerConverter converter;
    private final String bucketName;

    public VideoController(
            VideoPostingServicePort videoPostingService,
            VideoControllerConverter converter,
            @Value("${aws.s3.bucket.videos}") String bucketName
    ) {
        this.videoPostingService = videoPostingService;
        this.converter = converter;
        this.bucketName = bucketName;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadVideo(
            @RequestPart("file") MultipartFile file,
            @RequestPart("file_name") String fileName
            ) throws IOException {

        VideoPostingRequest request = converter.convertToVideoPostingRequest(fileName, "123", file);
        videoPostingService.upload(request, bucketName);
        return ResponseEntity.accepted().build();
    }

    @GetMapping()
    public ResponseEntity<List<VideoResponseDto>> getVideos(){
        List<VideoResponseDto> videos =  videoPostingService.getVideos("123");
        return ResponseEntity.ok(videos);
    }
}
