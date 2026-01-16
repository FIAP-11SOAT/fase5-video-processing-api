package com.example.demo.shared.constants;

import com.example.demo.shared.exceptions.ErrorType;
import com.example.demo.shared.exceptions.ExceptionUtils;

public class ApplicationConstants {

    private ApplicationConstants() {
        throw ExceptionUtils.internalError(ErrorType.UTILITY_CLASS_ERROR, null);
    }

    public static final String TABLE = "fase5-infra-hacka-video-processing";
}