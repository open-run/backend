package io.openur.domain.bung.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.Getter;

@Getter
public class EditBungDto {

    private String name;
    private String description;

    @Min(3)
    @Max(300)
    private Integer memberNumber;

    private Boolean hasAfterRun;
    private String afterRunDescription;
    private List<String> hashtags;
    private String mainImage;
}
