package io.openur.global.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private List<T> data;
    private int totalPages;
    private long totalElements;
    private boolean first;
    private boolean last;
    @JsonProperty("empty")
    private boolean isEmpty;

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
