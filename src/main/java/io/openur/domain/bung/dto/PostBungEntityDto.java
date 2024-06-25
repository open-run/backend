package io.openur.domain.bung.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.ToString;

@Getter @ToString
public class PostBungEntityDto {

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
    private Integer memberNumber;
    @NotNull
    private Boolean hasAfterRun;
    private String afterRunDescription;
}
