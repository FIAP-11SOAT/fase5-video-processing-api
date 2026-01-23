package com.example.demo.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.InputStream;

@AllArgsConstructor
@Getter
@Setter
public class S3File{

    InputStream inputStream;
    long contentLength;
    String fileName;
}
