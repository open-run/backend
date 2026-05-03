package io.openur.domain.NFT.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class NftAssetUrlResolver {

    private final String publicBaseUrl;

    public NftAssetUrlResolver(
        @Value("${openrun.gcs.public-base-url}") String publicBaseUrl
    ) {
        this.publicBaseUrl = trimTrailingSlash(publicBaseUrl);
    }

    public String resolve(String storedUrl, String storageKey) {
        if (StringUtils.hasText(storageKey)) {
            return publicBaseUrl + "/" + storageKey;
        }

        return StringUtils.hasText(storedUrl) ? storedUrl : null;
    }

    private String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
