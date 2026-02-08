package io.openur.domain.challenge.event;

import io.openur.domain.user.model.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OnUserRegistration {
    private final User user;
}
