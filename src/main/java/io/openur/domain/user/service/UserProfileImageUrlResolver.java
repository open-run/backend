package io.openur.domain.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class UserProfileImageUrlResolver {

    private final String publicBaseUrl;

    public UserProfileImageUrlResolver(
        @Value("${openrun.gcs.public-base-url}") String publicBaseUrl
    ) {
        this.publicBaseUrl = trimTrailingSlash(publicBaseUrl);
    }

    public String resolve(String storageKey) {
        if (!StringUtils.hasText(storageKey)) {
            return null;
        }

        return publicBaseUrl + "/" + storageKey;
    }

    private String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
