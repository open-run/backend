package io.openur.domain.userbung.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
        this.userId = userBung.getUser().getUserId();
        this.nickname = userBung.getUser().getNickname();
        this.likeCount = userBung.getUser().getFeedback();
        this.profileImageUrl = "";
        this.participationStatus = userBung.isParticipationStatus();
        this.isOwner = userBung.isOwner();
    }
}
