package io.openur.domain.NFT.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NftAssetUrlResolverTest {

    private static final String GCS_BASE = "https://storage.googleapis.com/openrun-nft";
    private static final String GATEWAY = "https://swarm-api.yjkellyjoo.dev";
    private static final String REF = "94a308cc28193a0af03e87d444e6feadd538275b5fa9d21ba6b11b56c7c4e01a";

    @Test
    @DisplayName("ref가 있으면 Swarm 게이트웨이 URL(/bzz/<ref>)로 변환한다")
    void resolve_returnsSwarmGatewayUrlWhenRefExists() {
        NftAssetUrlResolver resolver = new NftAssetUrlResolver(GCS_BASE, GATEWAY);

        assertThat(resolver.resolve(REF)).isEqualTo(GATEWAY + "/bzz/" + REF);
    }

    @Test
    @DisplayName("게이트웨이 URL의 끝 슬래시는 제거되어 이중 슬래시가 생기지 않는다")
    void resolve_trimsTrailingSlashOnGateway() {
        NftAssetUrlResolver resolver = new NftAssetUrlResolver(GCS_BASE, GATEWAY + "/");

        assertThat(resolver.resolve(REF)).isEqualTo(GATEWAY + "/bzz/" + REF);
    }

    @Test
    @DisplayName("ref가 없으면 null을 반환한다")
    void resolve_returnsNullWhenRefMissing() {
        NftAssetUrlResolver resolver = new NftAssetUrlResolver(GCS_BASE, GATEWAY);

        assertThat(resolver.resolve(null)).isNull();
        assertThat(resolver.resolve("")).isNull();
    }
}
