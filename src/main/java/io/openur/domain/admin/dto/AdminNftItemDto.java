package io.openur.domain.admin.dto;

import io.openur.domain.NFT.entity.NftEntity;
import io.openur.domain.NFT.service.NftAssetUrlResolver;
import io.openur.domain.NFT.service.NftAvatarItemViewMapper;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminNftItemDto {

    private String tokenId;
    private String name;
    private String category;
    private String mainCategory;
    private String subCategory;
    private String rarity;
    private String thumbnailUrl;

    public static AdminNftItemDto from(
        NftEntity nft,
        String tokenId,
        NftAssetUrlResolver assetUrlResolver,
        NftAvatarItemViewMapper viewMapper
    ) {
        return AdminNftItemDto.builder()
            .tokenId(tokenId)
            .name(nft.getName())
            .category(nft.getCategory().name())
            .mainCategory(viewMapper.getMainCategory(nft))
            .subCategory(viewMapper.getSubCategory(nft))
            .rarity(nft.getRarity().name())
            .thumbnailUrl(assetUrlResolver.resolve(nft.getThumbnailRef()))
            .build();
    }
}
