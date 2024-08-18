package io.openur.global.common;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Response<T> {
    private String message;
    private T data;
    private int totalPages = 0;
    private long totalElements = 0L;
    private boolean first = true;
    private boolean last = true;
    private boolean empty = true;
}
