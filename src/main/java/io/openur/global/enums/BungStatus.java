package io.openur.global.enums;

public enum BungStatus {
    ALL, // 전체
    AVAILABLE, // 참여 가능한
    JOINED, // 참여한
    FINISHED // 이미 종료된
    ;

    public static boolean isAvailable(BungStatus status) {
        return status == AVAILABLE;
    }

    public static boolean notUserFiltered(BungStatus status) {
        return status == ALL || status == FINISHED;
    }
}
