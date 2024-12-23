package io.openur.domain.user.exception;
import lombok.Getter;
import java.util.List;

@Getter
public class UserNotFoundException extends RuntimeException {
    private final List<String> notFoundUserIds;

    public UserNotFoundException(List<String> notFoundUserIds) {
        super("Some user IDs were not found");
        this.notFoundUserIds = notFoundUserIds;
    }
}
