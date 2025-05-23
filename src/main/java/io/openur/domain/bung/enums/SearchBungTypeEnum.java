package io.openur.domain.bung.enums;

public enum SearchBungTypeEnum {
    ALL,
    MEMBER_NAME,
    HASHTAG,
    LOCATION
    ;
    
    public static boolean needToSearch(SearchBungTypeEnum type) {
        return type != ALL;
    }
}
