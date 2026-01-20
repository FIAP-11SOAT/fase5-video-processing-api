package com.example.demo.adapters.outbound.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.time.OffsetDateTime;
import java.util.UUID;

@DynamoDbBean
public class VideoDynamoModel {

    private String videoKey;
    private UUID id;
    private String userId;
    private String name;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @DynamoDbPartitionKey
    public String getVideoKey() {
        return videoKey;
    }

    public void setVideoKey(String videoKey) {
        this.videoKey = videoKey;
    }

    public String getId() {
        return id != null ? id.toString() : null;
    }

    public void setId(String id) {
        this.id = UUID.fromString(id);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt != null ? createdAt.toString() : null;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = OffsetDateTime.parse(createdAt);
    }

    public String getUpdatedAt() {
        return updatedAt != null ? updatedAt.toString() : null;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = OffsetDateTime.parse(updatedAt);
    }
}
