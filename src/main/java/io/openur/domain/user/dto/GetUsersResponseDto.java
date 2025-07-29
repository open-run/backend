package io.openur.domain.user.dto;

import com.querydsl.core.Tuple;
import io.openur.domain.user.entity.UserEntity;
import io.openur.domain.user.model.User;
import lombok.Getter;

@Getter
public class GetUsersResponseDto {

    private final String nickname;
    private final String userId;
    private final String runningPace;
    private final Integer runningFrequency;
    private final Long collabCount;

    public GetUsersResponseDto(Tuple userCounts) {
        User user = User.from(userCounts.get(0, UserEntity.class));

        this.userId = user.getUserId();
        this.nickname = user.getNickname();
        this.runningPace = user.getRunningPace();
        this.runningFrequency = user.getRunningFrequency();
        this.collabCount = userCounts.get(1, Long.class);
    }
}
