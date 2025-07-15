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
    private String email;
    private String profileImageUrl;
    private Integer likeCount;
//    private String runningPace;

//    private Long userBungId;
    private boolean participationStatus;
    @JsonProperty("owner")
    private boolean isOwner;

    public UserBungInfoDto(UserBung userBung) {
        this.userId = userBung.getUser().getUserId();
        this.nickname = userBung.getUser().getNickname();
        this.email = userBung.getUser().getEmail();
        this.likeCount = userBung.getUser().getFeedback();
        this.profileImageUrl = "";
//        this.runningPace = userBung.getUser().getRunningPace();
//        this.userBungId = userBung.getUserBungId();
        this.participationStatus = userBung.isParticipationStatus();
        this.isOwner = userBung.isOwner();
    }
}
