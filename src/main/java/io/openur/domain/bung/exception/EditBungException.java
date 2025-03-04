package io.openur.domain.bung.exception;

import io.openur.domain.bung.enums.EditBungResultEnum;

public class EditBungException extends RuntimeException {

    public EditBungException(EditBungResultEnum editBungResultEnum) {
        super(editBungResultEnum.toString());
    }
}
