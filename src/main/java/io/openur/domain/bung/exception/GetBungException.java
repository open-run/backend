package io.openur.domain.bung.exception;

import io.openur.domain.bung.enums.GetBungResultEnum;

public class GetBungException extends RuntimeException {
    
    public GetBungException(GetBungResultEnum getBungResultEnum) {
        super(getBungResultEnum.toString());
    }
}
