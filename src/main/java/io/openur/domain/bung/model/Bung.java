package io.openur.domain.bung.model;

import io.openur.domain.bung.dto.PostBungEntityDto;
import io.openur.domain.bung.entity.BungEntity;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Bung {
    private String bungId;
    private String name;
    private String description;
    private String location;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Float distance;
    private String pace;
    private Integer memberNumber;
    private Boolean hasAfterRun;
    private String afterRunDescription;

    public Bung(PostBungEntityDto dto) {
        this.bungId = UUID.randomUUID().toString();
        this.name = dto.getName();
        this.description = dto.getDescription();
        this.location = dto.getLocation();
        this.startDateTime = dto.getStartDateTime();
        this.endDateTime = dto.getEndDateTime();
        this.distance = dto.getDistance();
        this.pace = dto.getPace();
        this.memberNumber = dto.getMemberNumber();
        this.hasAfterRun = dto.getHasAfterRun();
        this.afterRunDescription = dto.getAfterRunDescription();
    }

    public static Bung from(final BungEntity bungEntity) {
        return new Bung(
            bungEntity.getBungId(),
            bungEntity.getName(),
            bungEntity.getDescription(),
            bungEntity.getLocation(),
            bungEntity.getStartDateTime(),
            bungEntity.getEndDateTime(),
            bungEntity.getDistance(),
            bungEntity.getPace(),
            bungEntity.getMemberNumber(),
            bungEntity.getHasAfterRun(),
            bungEntity.getAfterRunDescription()
        );
    }

    public BungEntity toEntity() {
        return new BungEntity(
            bungId,
            name,
            description,
            location,
            startDateTime,
            endDateTime,
            distance,
            pace,
            memberNumber,
            hasAfterRun,
            afterRunDescription
        );
    }
}
