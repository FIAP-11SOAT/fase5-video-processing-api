package com.example.demo.adapters.dto.http;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record VideoPostingBodyDto(

        @NotNull
        @JsonProperty("file_name")
        String fileName
) {
}
