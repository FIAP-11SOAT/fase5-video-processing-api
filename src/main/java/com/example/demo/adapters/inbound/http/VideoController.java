package com.example.demo.adapters.inbound.http;

import com.example.demo.adapters.converter.VideoControllerConverter;
import com.example.demo.adapters.dto.VideoResponseDto;
import com.example.demo.core.model.VideoPostingRequest;
import com.example.demo.core.ports.VideoPostingServicePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
            @RequestPart("file_name") String fileName,
            @AuthenticationPrincipal Jwt jwt
            ) throws IOException {

        String userId;
        String userName;

        System.out.println(jwt.getClaims());

        if (jwt == null){
            userId = "123";
            userName = "user";
        } else {
            userId = jwt.getClaims().get("sub").toString();
            userName = jwt.getClaims().get("username").toString();
        }

        VideoPostingRequest request = converter.convertToVideoPostingRequest(fileName, userId, userName, file);
        videoPostingService.upload(request, bucketName);
        return ResponseEntity.accepted().build();
    }

    @GetMapping()
    public ResponseEntity<List<VideoResponseDto>> getVideos(
            @AuthenticationPrincipal Jwt jwt
    ){

        String userId;
        String userName;

        if (jwt == null){
            userId = "123";
            userName = "user";
        } else {
            userId = jwt.getClaims().get("sub").toString();
            userName = jwt.getClaims().get("username").toString();
        }
        List<VideoResponseDto> videos =  videoPostingService.getVideos(userId);
        return ResponseEntity.ok(videos);
    }
}
