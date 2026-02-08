package io.openur.domain.challenge.event;

import io.openur.domain.userchallenge.model.UserChallenge;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OnIssue {
    private final UserChallenge userChallenge;
}
