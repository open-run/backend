package io.openur.domain.userbung.dto;

import io.openur.domain.userbung.model.UserBung;
import lombok.Getter;

@Getter
public class UserBungInfoDto {
    private String userId;
    private String nickname;
    private String email;
    private String runningPace;

    private Long userBungId;
    private boolean participationstatus;
    private boolean isOwner;

    public UserBungInfoDto(UserBung userBung) {
        this.userId = userBung.getUser().getUserId();
        this.nickname = userBung.getUser().getNickname();
        this.email = userBung.getUser().getEmail();
        this.runningPace = userBung.getUser().getRunningPace();
        this.userBungId = userBung.getUserBungId();
        this.participationstatus = userBung.isParticipationStatus();
        this.isOwner = userBung.isOwner();
    }
}
