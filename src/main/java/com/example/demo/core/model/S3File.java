package com.example.demo.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.InputStream;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class S3File{

    InputStream inputStream;
    long contentLength;
    String fileName;
}
