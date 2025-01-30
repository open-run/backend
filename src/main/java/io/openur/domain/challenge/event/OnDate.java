package io.openur.domain.challenge.event;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OnDate {
    private final List<String> userIds;
    private final LocalDateTime completedConditionDate;
}
