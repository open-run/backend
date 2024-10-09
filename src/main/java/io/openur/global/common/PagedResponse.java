package io.openur.global.common;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@AllArgsConstructor
public class PagedResponse<T> {
    private String message;
    private List<T> data = new ArrayList<>();
    private int totalPages = 0;
    private long totalElements = 0;
    private boolean first = true;
    private boolean last = true;
    private boolean isEmpty = true;

    public static <T> PagedResponse<T> build(Page<T> data, String message) {
        return new PagedResponse<>(
            message,
            data.getContent(),
            data.getTotalPages(),
            data.getTotalElements(),
            data.isFirst(),
            data.isLast(),
            data.isEmpty()
        );
    }
}
