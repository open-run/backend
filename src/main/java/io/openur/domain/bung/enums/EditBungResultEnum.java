package io.openur.domain.bung.enums;

public enum EditBungResultEnum {
    SUCCESSFULLY_EDITED("successfully edited"),
    BUNG_HAS_ALREADY_COMPLETED("You cannot edit bung - bung has already completed"),
    BUNG_HAS_ALREADY_STARTED("You cannot edit bung - bung has already started"),
    ;

    private final String value;

    EditBungResultEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
