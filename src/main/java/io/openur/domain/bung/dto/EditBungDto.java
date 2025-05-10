package io.openur.domain.bung.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;

@Getter
public class EditBungDto {

    @NotBlank
    private String name;
    @NotBlank
    private String description;
    
    private String mainImage;
    
    @Min(3)
    @Max(300)
    @NotNull
    private Integer memberNumber;
    
    @NotNull
    private Boolean hasAfterRun;
    private String afterRunDescription;
    
    private List<String> hashtags;
}
