package io.openur.global.common;

import org.springframework.http.HttpStatus;

public class Exception {
    private int statusCode;     // 프론트에서 처리 편의성을 위해
    private HttpStatus state;
    private String message;

}
