package io.openur.global.enums;

public enum CompleteBungResultEnum {
    SUCCESSFULLY_COMPLETED("successfully completed"),
    BUNG_HAS_ALREADY_COMPLETED("bung has already completed"),
    BUNG_HAS_NOT_STARTED("bung has not started yet"),
    ;

    private final String value;

    CompleteBungResultEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
