package com.campushub.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final Map<String, HttpStatus> AUTHORITY_ERROR_CODES = Map.of(
        "无管理员权限", HttpStatus.FORBIDDEN,
        "账号已被禁用", HttpStatus.FORBIDDEN
    );

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException exception) {
        String message = exception.getMessage();
        HttpStatus status = HttpStatus.BAD_REQUEST;

        for (var entry : AUTHORITY_ERROR_CODES.entrySet()) {
            if (message != null && message.contains(entry.getKey())) {
                status = entry.getValue();
                break;
            }
        }

        if (status == HttpStatus.BAD_REQUEST) {
            log.warn("Bad request: {}", message);
        } else {
            log.warn("Access denied [{}]: {}", status.value(), message);
        }

        return ResponseEntity
            .status(status)
            .body(Map.of("message", message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception exception) {
        log.error("Unexpected server error", exception);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("message", "服务器开小差了，请稍后重试"));
    }
}
