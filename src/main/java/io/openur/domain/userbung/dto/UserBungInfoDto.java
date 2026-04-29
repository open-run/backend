package io.openur.domain.userbung.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.openur.domain.user.service.UserProfileImageUrlResolver;
import io.openur.domain.userbung.entity.UserBungEntity;
import io.openur.domain.userbung.model.UserBung;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserBungInfoDto {

    private String userId;
    private String nickname;
    private String profileImageUrl;
    private Integer likeCount;
    private boolean participationStatus;
    @JsonProperty("owner")
    private boolean isOwner;

    public UserBungInfoDto(UserBung userBung) {
        this(userBung, null);
    }

    public UserBungInfoDto(UserBung userBung, UserProfileImageUrlResolver profileImageUrlResolver) {
        this.userId = userBung.getUser().getUserId();
        this.nickname = userBung.getUser().getNickname();
        this.likeCount = userBung.getUser().getFeedback();
        this.profileImageUrl = resolveProfileImageUrl(
            profileImageUrlResolver,
            userBung.getUser().getProfileImageStorageKey()
        );
        this.participationStatus = userBung.isParticipationStatus();
        this.isOwner = userBung.isOwner();
    }

    public UserBungInfoDto(UserBungEntity userBungEntity) {
        this(userBungEntity, null);
    }

    public UserBungInfoDto(UserBungEntity userBungEntity, UserProfileImageUrlResolver profileImageUrlResolver) {
        this.userId = userBungEntity.getUserEntity().getUserId();
        this.nickname = userBungEntity.getUserEntity().getNickname();
        this.likeCount = userBungEntity.getUserEntity().getFeedback();
        this.profileImageUrl = resolveProfileImageUrl(
            profileImageUrlResolver,
            userBungEntity.getUserEntity().getProfileImageStorageKey()
        );
        this.participationStatus = userBungEntity.isParticipationStatus();
        this.isOwner = userBungEntity.isOwner();
    }

    private String resolveProfileImageUrl(
        UserProfileImageUrlResolver profileImageUrlResolver,
        String profileImageStorageKey
    ) {
        return profileImageUrlResolver == null ? null : profileImageUrlResolver.resolve(profileImageStorageKey);
    }
}
