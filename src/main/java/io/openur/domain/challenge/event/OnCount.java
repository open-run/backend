package io.openur.domain.challenge.event;

import io.openur.domain.userchallenge.model.UserChallenge;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class OnCount {
    private final List<UserChallenge> userChallenges;
}
