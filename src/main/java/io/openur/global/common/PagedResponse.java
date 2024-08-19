package io.openur.global.common;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
    private String message;
    private List<T> data = new ArrayList<>();
    private int totalPages = 0;
    private long totalElements = 0L;
    private boolean first = true;
    private boolean last = true;
    private boolean empty = true;

    public static <T> PagedResponse build(String message, Page<T> page) {
        return new PagedResponse(message, page.getContent(), page.getTotalPages(), page.getTotalElements(), page.isFirst(), page.isLast(), page.isEmpty());
    }
}
