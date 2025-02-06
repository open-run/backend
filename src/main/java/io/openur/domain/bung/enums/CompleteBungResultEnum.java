package io.openur.domain.bung.enums;

public enum CompleteBungResultEnum {
    SUCCESSFULLY_COMPLETED("successfully completed"),
    BUNG_HAS_ALREADY_COMPLETED("You cannot complete bung - bung has already completed"),
    BUNG_HAS_NOT_STARTED("You cannot complete bung - bung has not started yet"),
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
