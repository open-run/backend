package io.openur.global.common;

import java.net.URI;
import java.util.function.Consumer;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public class UtilController {

    public static URI createUri(String todoId) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(todoId)
            .toUri();
    }

    public static <T> void applyIfNotNull(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
