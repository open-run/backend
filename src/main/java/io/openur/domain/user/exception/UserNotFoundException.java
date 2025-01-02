package io.openur.domain.user.exception;

import java.util.List;
import lombok.Getter;

@Getter
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(List<String> notFoundUserIds) {
        super("Some user IDs were not found: [" + String.join(", ", notFoundUserIds + "]"));
    }
}
