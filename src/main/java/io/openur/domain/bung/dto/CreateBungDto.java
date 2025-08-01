package io.openur.domain.bung.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CreateBungDto {

    @NotBlank
    private String name;
    private String description;
    private String mainImage;
    @NotBlank
    private String location;
    @NotNull
    private Double latitude;
    @NotNull
    private Double longitude;
    @NotNull
    private LocalDateTime startDateTime;
    @NotNull
    private LocalDateTime endDateTime;
    @NotNull
    private Float distance;
    @NotBlank
    private String pace;

    @NotNull
    @Min(3)
    @Max(300)
    private Integer memberNumber;

    @NotNull
    private Boolean hasAfterRun;
    private String afterRunDescription;
    private List<String> hashtags = Collections.emptyList();
}
