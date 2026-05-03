package io.openur.domain.NFT.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NftAssetUrlResolverTest {

    @Test
    @DisplayName("storage key가 있으면 GCS public URL로 변환한다")
    void resolve_returnsGcsPublicUrlWhenStorageKeyExists() {
        NftAssetUrlResolver resolver = new NftAssetUrlResolver(
            "https://storage.googleapis.com/openrun-nft/"
        );

        String resolvedUrl = resolver.resolve(
            null,
            "nft-assets/v1/nft-items/hair/57/equip/front.png"
        );

        assertThat(resolvedUrl)
            .isEqualTo("https://storage.googleapis.com/openrun-nft/nft-assets/v1/nft-items/hair/57/equip/front.png");
    }

    @Test
    @DisplayName("storage key가 없으면 stored URL로 fallback한다")
    void resolve_fallbackToStoredUrlWhenStorageKeyMissing() {
        NftAssetUrlResolver resolver = new NftAssetUrlResolver(
            "https://storage.googleapis.com/openrun-nft"
        );

        assertThat(resolver.resolve("https://cdn.example.com/item.png", null))
            .isEqualTo("https://cdn.example.com/item.png");
        assertThat(resolver.resolve(null, null)).isNull();
    }
}
