package io.openur.domain.bung.model;

import static io.openur.global.common.UtilController.applyIfNotNull;

import io.openur.domain.bung.dto.BungInfoDto;
import io.openur.domain.bung.dto.CreateBungDto;
import io.openur.domain.bung.dto.EditBungDto;
import io.openur.domain.bung.entity.BungEntity;
import io.openur.domain.hashtag.entity.HashtagEntity;
import java.time.LocalDateTime;
import java.util.List;
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
    private List<String> hashtags;
    private boolean isCompleted;
    private String mainImage;

    public Bung(CreateBungDto dto) {
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
        this.mainImage = dto.getMainImage();
    }

    public Bung(BungInfoDto dto) {
        this.bungId = dto.getBungId();
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
        this.mainImage = dto.getMainImage();
    }

    public static Bung from(final BungEntity bungEntity) {
        Bung bung = new Bung(
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
            bungEntity.getAfterRunDescription(),
            null,
            bungEntity.isCompleted(),
            bungEntity.getMainImage()
        );

        if (bungEntity.getHashtags() != null) {
            bung.hashtags = bungEntity.getHashtags()
                .stream()
                .map(HashtagEntity::getHashtagStr)
                .toList();
        }

        return bung;
    }

    public void update(EditBungDto dto) {
        applyIfNotNull(dto.getName(), newName -> this.name = newName);
        applyIfNotNull(dto.getDescription(), newDesc -> this.description = newDesc);
        applyIfNotNull(dto.getMemberNumber(), newNumber -> this.memberNumber = newNumber);
        applyIfNotNull(dto.getHasAfterRun(), newAfterRun -> this.hasAfterRun = newAfterRun);
        applyIfNotNull(
            dto.getAfterRunDescription(),
            newAfterRunDesc -> this.afterRunDescription = newAfterRunDesc);
        applyIfNotNull(dto.getMainImage(), newMainImage -> this.mainImage = newMainImage);
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
            afterRunDescription,
            isCompleted,
            mainImage,
            null
        );
    }

    public void completeBung() {
        this.isCompleted = true;
    }
}
