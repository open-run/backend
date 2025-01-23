package io.openur.domain.bung.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@ToString
public class CreateBungDto {

    @NotBlank
    private String name;
    private String description;
    @NotBlank
    private String location;
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
    private List<String> hashtags;
}
