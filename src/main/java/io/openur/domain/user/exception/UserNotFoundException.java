package io.openur.domain.user.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(List<String> notFoundUserIds) {
        super("Some user IDs were not found: [" + String.join(", ", notFoundUserIds + "]"));
    }
}
