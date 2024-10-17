package io.openur.domain.user.dto;

import com.querydsl.core.Tuple;
import io.openur.domain.user.entity.UserEntity;
import io.openur.domain.user.model.User;
import lombok.Getter;

@Getter
public class GetUsersResponseDto {
    private String userId;
    private final String nickname;
    private final String email;
    private String runningPace;
    private Integer runningFrequency;
    private Long collabCount;

    public GetUsersResponseDto(Tuple userCounts) {
        User user = User.from(userCounts.get(0, UserEntity.class));

        this.userId = user.getUserId();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.runningPace = user.getRunningPace();
        this.runningFrequency = user.getRunningFrequency();
        this.collabCount = userCounts.get(1, Long.class);
    }
}
