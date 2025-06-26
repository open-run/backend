package io.openur.domain.bung.exception;

import io.openur.domain.bung.enums.SearchBungResultEnum;

public class SearchBungException extends RuntimeException {
    
    public SearchBungException(SearchBungResultEnum searchBungResultEnum) {
        super(searchBungResultEnum.toString());
    }
}
