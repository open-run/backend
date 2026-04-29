package io.openur.domain.bung.enums;

public enum BungSearchCategory {
    NAME("이름"),
    MEMBER("멤버"),
    HASHTAG("해시태그"),
    LOCATION("위치");

    private final String label;

    BungSearchCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
