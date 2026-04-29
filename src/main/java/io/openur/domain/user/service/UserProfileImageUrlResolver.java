package io.openur.domain.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class UserProfileImageUrlResolver {

    private final boolean serveLocalNftAssets;
    private final String localAssetBaseUrl;

    public UserProfileImageUrlResolver(
        @Value("${openrun.nft.assets.serve-local:false}") boolean serveLocalNftAssets,
        @Value("${openrun.nft.assets.public-base-url:http://localhost:8080/local-nft-assets}") String localAssetBaseUrl
    ) {
        this.serveLocalNftAssets = serveLocalNftAssets;
        this.localAssetBaseUrl = trimTrailingSlash(localAssetBaseUrl);
    }

    public String resolve(String storageKey) {
        if (!serveLocalNftAssets || !StringUtils.hasText(storageKey)) {
            return null;
        }

        return localAssetBaseUrl + "/" + storageKey;
    }

    private String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
