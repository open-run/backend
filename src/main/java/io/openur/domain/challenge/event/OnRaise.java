package io.openur.domain.challenge.event;

import io.openur.domain.userchallenge.model.UserChallenge;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OnRaise {
    private final List<UserChallenge> userChallenges;
}
