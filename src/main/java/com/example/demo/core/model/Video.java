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

    private String videoKey;
    private UUID id;
    private String userId;
    private String name;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
