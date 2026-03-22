package com.example.training.exception;

import com.example.training.dto.CommonResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public CommonResponse<Void> handleException(Exception exception) {
        return CommonResponse.failure(exception.getMessage() == null ? "internal error" : exception.getMessage());
    }
}
