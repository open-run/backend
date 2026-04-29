package io.openur.domain.bung.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BungSearchResponseDto {

    private String keyword;
    private List<BungSearchCategoryResultDto> categories;
}
