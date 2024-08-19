package io.openur.global.common;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Response<T> {
    private String message;
    private T data;
}
