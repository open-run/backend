package io.openur.domain.bung.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.openur.domain.bung.model.Bung;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BungInfoDto {
    private String bungId;
    private String name;
    private String description;
    private String location;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDateTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDateTime;
    private Float distance;
    private String pace;
    private Integer memberNumber;
    private Boolean hasAfterRun;
    private String afterRunDescription;
    private Boolean isCompleted;
    private List<String> hashtags;

    public BungInfoDto(Bung bung) {
        this.bungId = bung.getBungId();
        this.name = bung.getName();
        this.description = bung.getDescription();
        this.location = bung.getLocation();
        this.startDateTime = bung.getStartDateTime();
        this.endDateTime = bung.getEndDateTime();
        this.distance = bung.getDistance();
        this.pace = bung.getPace();
        this.memberNumber = bung.getMemberNumber();
        this.hasAfterRun = bung.getHasAfterRun();
        this.afterRunDescription = bung.getAfterRunDescription();
        this.isCompleted = bung.isCompleted();
        this.hashtags = bung.getHashtags();
    }

    public BungInfoDto(Bung bung, List<String> hashtags) {
        this(bung);
        this.hashtags = hashtags;
    }
}
