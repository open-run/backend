package io.openur.domain.NFT.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class NftAssetUrlResolver {

    private final boolean serveLocalNftAssets;
    private final String localAssetBaseUrl;

    public NftAssetUrlResolver(
        @Value("${openrun.nft.assets.serve-local:false}") boolean serveLocalNftAssets,
        @Value("${openrun.nft.assets.public-base-url:http://localhost:8080/local-nft-assets}") String localAssetBaseUrl
    ) {
        this.serveLocalNftAssets = serveLocalNftAssets;
        this.localAssetBaseUrl = trimTrailingSlash(localAssetBaseUrl);
    }

    public String resolve(String storedUrl, String storageKey) {
        if (serveLocalNftAssets && StringUtils.hasText(storageKey)) {
            return localAssetBaseUrl + "/" + storageKey;
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
