package io.openur.domain.bung.exception;

import io.openur.domain.bung.enums.CompleteBungResultEnum;

public class CompleteBungException extends RuntimeException {

    public CompleteBungException(CompleteBungResultEnum completeBungResultEnum) {
        super(completeBungResultEnum.toString());
    }
}
