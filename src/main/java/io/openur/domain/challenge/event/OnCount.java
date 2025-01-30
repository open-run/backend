package io.openur.domain.challenge.event;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OnCount {
    private final List<String> userIds;
    private final Long completedConditionCount;
}
