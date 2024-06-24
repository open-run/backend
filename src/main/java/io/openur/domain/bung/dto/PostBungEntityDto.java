package io.openur.domain.bung.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.ToString;

@Getter @ToString
public class PostBungEntityDto {
    @NotEmpty
    private String name;
    private String description;
    @NotEmpty
    private String location;
    @NotNull
    private LocalDateTime startDateTime;
    @NotNull
    private LocalDateTime endDateTime;
    @NotNull
    private Float distance;
    @NotEmpty
    private String pace;
    @NotNull
    private Integer memberNumber;
    @NotNull
    private Boolean hasAfterRun;
    private String afterRunDescription;
}
