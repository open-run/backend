package io.openur.domain.bung.enums;

public enum GetBungResultEnum {
    BUNG_NOT_FOUND("Bung not found"),
    EMPTY_KEYWORD("No keyword has been provided for search request")
    ;
    
    private final String value;
    
    GetBungResultEnum(String value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return value;
    }
}
