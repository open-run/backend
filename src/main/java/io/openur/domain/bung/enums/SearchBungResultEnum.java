package io.openur.domain.bung.enums;

public enum SearchBungResultEnum {
    EMPTY_KEYWORD("No keyword has been provided for search request"),
    NO_LOCATION_SPECIFIED("You need to submit the name of location"),
    NO_NICKNAME_PROVIDED("You need to submit the desired users nickname"),
    NO_HASHTAG_PROVIDED("You need to provide at least one hashtag")
    ;
    
    private final String value;
    
    SearchBungResultEnum(String value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return value;
    }
}
