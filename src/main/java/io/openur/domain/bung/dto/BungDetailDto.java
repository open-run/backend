package io.openur.domain.bung.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.userbung.model.UserBung;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BungDetailDto {
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
    private Boolean isOwner;

    public BungDetailDto(Bung bung) {
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
    }

    public BungDetailDto(UserBung userBung) {
        this(userBung.getBung());
        this.isOwner = userBung.getBung() != null;
    }
}
