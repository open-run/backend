package io.openur.domain.NFT.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class NftAssetUrlResolver {

    private final String swarmGatewayUrl;

    public NftAssetUrlResolver(
        @Value(
            "${nft.swarm-gateway-url:https://api.gateway.ethswarm.org}"
        ) String swarmGatewayUrl
    ) {
        this.swarmGatewayUrl = trimTrailingSlash(swarmGatewayUrl);
    }

    /**
     * Builds the public Swarm gateway URL for a catalog image. A {@code ref} is a
     * bare, complete Swarm reference; the resulting {@code {gateway}/bzz/{ref}} is
     * the single canonical form — never append a path to it.
     */
    public String resolve(String ref) {
        return StringUtils.hasText(ref)
            ? swarmGatewayUrl + "/bzz/" + ref
            : null;
    }

    private String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return value.endsWith("/")
            ? value.substring(0, value.length() - 1)
            : value;
    }
}
