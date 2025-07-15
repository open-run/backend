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
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter 
@Setter
@AllArgsConstructor
public class Bung {

    private String bungId;
    private String name;
    private String description;
    private String mainImage;
    private String location;
    private Double latitude;
    private Double longitude;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Float distance;
    private String pace;
    private Integer memberNumber;
    private Integer currentMemberNumber;
    private Boolean hasAfterRun;
    private String afterRunDescription;
    private boolean isCompleted;
    private List<String> hashtags;

    public Bung(CreateBungDto dto) {
        this.bungId = UUID.randomUUID().toString();
        this.name = dto.getName();
        this.description = dto.getDescription();
        this.location = dto.getLocation();
        this.latitude = dto.getLatitude();
        this.longitude = dto.getLongitude();
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
        this.latitude = dto.getLatitude();
        this.longitude = dto.getLongitude();
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
        return Bung.builder()
            .bungId(bungEntity.getBungId())
            .name(bungEntity.getName())
            .description(bungEntity.getDescription())
            .location(bungEntity.getLocation())
            .latitude(bungEntity.getLatitude())
            .longitude(bungEntity.getLongitude())
            .startDateTime(bungEntity.getStartDateTime())
            .endDateTime(bungEntity.getEndDateTime())
            .distance(bungEntity.getDistance())
            .pace(bungEntity.getPace())
            .memberNumber(bungEntity.getMemberNumber())
            .currentMemberNumber(bungEntity.getCurrentMemberNumber())
            .hasAfterRun(bungEntity.getHasAfterRun())
            .afterRunDescription(bungEntity.getAfterRunDescription())
            .isCompleted(bungEntity.isCompleted())
            .mainImage(bungEntity.getMainImage())
            .hashtags(
                bungEntity.getBungHashtags().stream()
                .map(BungHashtagEntity::getHashtagEntity)
                .map(HashtagEntity::toString)
                .toList()
            )
            .build();
    }

    public BungEntity toEntity(List<BungHashtag> bungHashtags) {
        return BungEntity.builder()
            .bungId(bungId)
            .name(name)
            .description(description)
            .location(location)
            .latitude(latitude)
            .longitude(longitude)
            .startDateTime(startDateTime)
            .endDateTime(endDateTime)
            .distance(distance)
            .pace(pace)
            .memberNumber(memberNumber)
            .hasAfterRun(hasAfterRun)
            .afterRunDescription(afterRunDescription)
            .completed(isCompleted)
            .mainImage(mainImage)
            .bungHashtags(bungHashtags.stream().map(BungHashtag::toEntity).toList())
            .build();
    }

    public void completeBung() {
        this.isCompleted = true;
    }
}
