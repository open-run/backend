package io.openur.domain.bung.enums;

    public enum JoinBungResultEnum {
    SUCCESSFULLY_JOINED("successfully joined"),
    BUNG_HAS_ALREADY_STARTED("You cannot join bung - bung has already started"),
    BUNG_IS_FULL("You cannot join bung - bung is full"),
    USER_HAS_ALREADY_JOINED("You cannot join bung - user has already joined"),
    ;

    private final String value;

    JoinBungResultEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
