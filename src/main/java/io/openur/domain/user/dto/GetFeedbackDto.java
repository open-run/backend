package io.openur.domain.user.dto;

import java.util.List;
import lombok.Getter;

@Getter
public class GetFeedbackDto {

    private List<String> targetUserIds;

}
