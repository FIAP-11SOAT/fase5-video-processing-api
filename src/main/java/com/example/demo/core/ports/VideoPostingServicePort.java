package com.example.demo.core.ports;

import com.example.demo.adapters.dto.VideoResponseDto;
import com.example.demo.core.model.VideoPostingRequest;

import java.io.IOException;
import java.util.List;

public interface VideoPostingServicePort {

    void upload(VideoPostingRequest request) throws IOException;
    List<VideoResponseDto> getVideos(String userId);
}
