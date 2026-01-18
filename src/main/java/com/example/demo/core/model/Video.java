package com.example.demo.core.model;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Video {

    private UUID id;
    private String userId;
    private String videoKey;
    private String name;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
