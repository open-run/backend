package io.openur.global.enums;

public enum BungStatus {
    ALL, // 전체
    AVAILABLE, // 참여 가능한
    PENDING, // 참여했으며, 시작이전임
    ACHIEVED, // 참여했으며, 종료됨
    ;

    public static boolean isAvailable(BungStatus status) {
        return status == AVAILABLE;
    }

    public static boolean isPending(BungStatus status) {
        return status == PENDING;
    }

    public static boolean notUserFiltered(BungStatus status) {
        return status == ALL;
    }
}
