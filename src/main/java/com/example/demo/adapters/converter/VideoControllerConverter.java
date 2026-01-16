package com.example.demo.adapters.converter;

import com.example.demo.adapters.dto.http.VideoPostingBodyDto;
import com.example.demo.core.model.VideoPostingRequest;
import org.springframework.stereotype.Component;

@Component
public class VideoControllerConverter {

    public VideoPostingRequest convertToVideoPostingRequest(VideoPostingBodyDto dto, String userId){
        return new VideoPostingRequest(dto.fileName(), userId);
    }
}
