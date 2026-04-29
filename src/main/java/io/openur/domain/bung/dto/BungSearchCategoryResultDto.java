package io.openur.domain.bung.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.openur.domain.bung.enums.BungSearchCategory;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Getter
@NoArgsConstructor
public class BungSearchCategoryResultDto {

    private BungSearchCategory category;
    private String label;
    private List<BungInfoWithMemberListDto> data;
    private int totalPages;
    private long totalElements;
    private boolean first;
    private boolean last;
    @JsonProperty("empty")
    private boolean isEmpty;

    public BungSearchCategoryResultDto(
        BungSearchCategory category,
        Page<BungInfoWithMemberListDto> page
    ) {
        this.category = category;
        this.label = category.getLabel();
        this.data = page.getContent();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.first = page.isFirst();
        this.last = page.isLast();
        this.isEmpty = page.isEmpty();
    }
}
