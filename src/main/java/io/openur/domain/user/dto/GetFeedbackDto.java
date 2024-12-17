package io.openur.domain.user.dto;
import lombok.Getter;

import java.util.List;

public class GetFeedbackDto {
    private String bungId;
    private String userId;
    @Getter
    private List<String> targetUserId;

}
