package io.openur.domain.bung.exception;

import io.openur.domain.bung.enums.JoinBungResultEnum;

public class JoinBungException extends RuntimeException {

    public JoinBungException(JoinBungResultEnum joinBungResultEnum) {
        super(joinBungResultEnum.toString());
    }
}
