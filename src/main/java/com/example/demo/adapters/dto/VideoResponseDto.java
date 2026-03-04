package com.example.demo.adapters.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record VideoResponseDto(
        String videoKey,
        UUID id,
        String userId,
        String name,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
