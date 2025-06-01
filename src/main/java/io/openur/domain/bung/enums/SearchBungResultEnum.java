package io.openur.domain.bung.enums;

public enum SearchBungResultEnum {
    EMPTY_KEYWORD("No keyword has been provided for search request")
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
