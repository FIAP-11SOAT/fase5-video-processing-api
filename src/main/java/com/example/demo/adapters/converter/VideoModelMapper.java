package com.example.demo.adapters.converter;

import com.example.demo.adapters.outbound.model.VideoDynamoModel;
import com.example.demo.core.model.Video;

import java.time.OffsetDateTime;
import java.util.UUID;

public class VideoModelMapper {
    public static VideoDynamoModel toDynamo(Video video) {
        VideoDynamoModel model = new VideoDynamoModel();
        model.setId(video.getId().toString());
        model.setUserId(video.getUserId());
        model.setVideoKey(video.getVideoKey());
        model.setName(video.getName());
        model.setStatus(video.getStatus());
        model.setCreatedAt(video.getCreatedAt().toString());
        model.setUpdatedAt(video.getUpdatedAt().toString());
        return model;
    }

    public static Video toDomain(VideoDynamoModel model) {
        return new Video(
                model.getVideoKey(),
                UUID.fromString(model.getId()),
                model.getUserId(),
                model.getName(),
                model.getStatus(),
                OffsetDateTime.parse(model.getCreatedAt()),
                OffsetDateTime.parse(model.getUpdatedAt())
        );
    }
}
