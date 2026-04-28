package io.openur.domain.NFT.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NftAssetUrlResolverTest {

    @Test
    @DisplayName("local asset 서빙이 켜져 있으면 storage key를 localhost URL로 변환한다")
    void resolve_localAssetUrl() {
        NftAssetUrlResolver resolver = new NftAssetUrlResolver(
            true,
            "http://localhost:8080/local-nft-assets/"
        );

        String resolvedUrl = resolver.resolve(
            null,
            "nft-assets/v1/nft-items/hair/57/equip/front.png"
        );

        assertThat(resolvedUrl)
            .isEqualTo("http://localhost:8080/local-nft-assets/nft-assets/v1/nft-items/hair/57/equip/front.png");
    }

    @Test
    @DisplayName("local asset 서빙이 꺼져 있으면 DB URL이 있을 때만 반환한다")
    void resolve_storedUrlOnlyWhenLocalAssetDisabled() {
        NftAssetUrlResolver resolver = new NftAssetUrlResolver(
            false,
            "http://localhost:8080/local-nft-assets"
        );

        assertThat(resolver.resolve("https://cdn.example.com/item.png", "storage/key.png"))
            .isEqualTo("https://cdn.example.com/item.png");
        assertThat(resolver.resolve(null, "storage/key.png")).isNull();
    }
}
