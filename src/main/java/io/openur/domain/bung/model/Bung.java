package io.openur.domain.bung.model;

import static io.openur.global.common.UtilController.applyIfNotNull;

import io.openur.domain.bung.dto.BungInfoDto;
import io.openur.domain.bung.dto.CreateBungDto;
import io.openur.domain.bung.dto.EditBungDto;
import io.openur.domain.bung.entity.BungEntity;
import io.openur.domain.bunghashtag.entity.BungHashtagEntity;
import io.openur.domain.bunghashtag.model.BungHashtag;
import io.openur.domain.hashtag.entity.HashtagEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class Bung {

    private String bungId;
    private String name;
    private String description;
    private String mainImage;
    private String location;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Float distance;
    private String pace;
    private Integer memberNumber;
    private Boolean hasAfterRun;
    private String afterRunDescription;
    private boolean isCompleted;
    private List<String> hashtags;

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
    
    // 기본적으로 bung hashtag 는 엔티티기 때문에, 해당 메서드로 업데이트 불가
    public void update(EditBungDto dto) {
        applyIfNotNull(dto.getName(), this::setName);
        applyIfNotNull(dto.getDescription(), this::setDescription);
        applyIfNotNull(dto.getMainImage(), this::setMainImage);
        applyIfNotNull(dto.getMemberNumber(), this::setMemberNumber);
        applyIfNotNull(dto.getHasAfterRun(), this::setHasAfterRun);
        applyIfNotNull(dto.getAfterRunDescription(), this::setAfterRunDescription);
    }
    
    public static Bung from(final BungEntity bungEntity) {
        return new Bung(
            bungEntity.getBungId(),
            bungEntity.getName(),
            bungEntity.getDescription(),
            bungEntity.getMainImage(),
            bungEntity.getLocation(),
            bungEntity.getStartDateTime(),
            bungEntity.getEndDateTime(),
            bungEntity.getDistance(),
            bungEntity.getPace(),
            bungEntity.getMemberNumber(),
            bungEntity.getHasAfterRun(),
            bungEntity.getAfterRunDescription(),
            bungEntity.isCompleted(),
            bungEntity.getBungHashtags().stream()
                .map(BungHashtagEntity::getHashtagEntity)
                .map(HashtagEntity::getHashtagStr)
                .toList()
        );
    }
    
    public BungEntity toEntity(List<BungHashtag> bungHashtags) {
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
            bungHashtags.stream().map(BungHashtag::toEntity).toList()
        );
    }

    public void completeBung() {
        this.isCompleted = true;
    }
}
