package io.openur.domain.bung.model;

public enum BungStatus {
    AVAILABLE,
    PENDING,
    ACCOMPLISHED,
    ;

    public static boolean hasJoined(BungStatus status) {
        return status == PENDING || status == ACCOMPLISHED;
    }
}
