package io.openur.domain.NFT.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class NftAssetUrlResolver {

    private final String publicBaseUrl;
    private final String swarmGatewayUrl;

    public NftAssetUrlResolver(
        @Value("${openrun.gcs.public-base-url}") String publicBaseUrl,
        @Value("${swarm.gateway-url:https://swarm-api.yjkellyjoo.dev}") String swarmGatewayUrl
    ) {
        this.publicBaseUrl = trimTrailingSlash(publicBaseUrl);
        this.swarmGatewayUrl = trimTrailingSlash(swarmGatewayUrl);
    }

    /**
     * Builds the public Swarm gateway URL for a catalog image. A {@code ref} is a
     * bare, complete Swarm reference; the resulting {@code {gateway}/bzz/{ref}} is
     * the single canonical form — never append a path to it.
     */
    public String resolve(String ref) {
        return StringUtils.hasText(ref) ? swarmGatewayUrl + "/bzz/" + ref : null;
    }

    /**
     * Legacy GCS resolution (storageKey path under the public bucket, with a stored
     * URL fallback). Retained only until all NFT callers migrate to Swarm refs.
     *
     * @deprecated use {@link #resolve(String)} with a Swarm reference.
     */
    @Deprecated
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
