package io.openur.domain.admin.dto;

import io.openur.domain.NFT.entity.NftItemEntity;
import io.openur.domain.NFT.service.NftAssetUrlResolver;
import io.openur.domain.NFT.service.NftAvatarItemViewMapper;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminNftItemDto {

    private Long nftItemId;
    private String tokenId;
    private String name;
    private String category;
    private String mainCategory;
    private String subCategory;
    private String rarity;
    private String thumbnailStorageKey;
    private String thumbnailUrl;

    public static AdminNftItemDto from(
        NftItemEntity nftItem,
        NftAssetUrlResolver assetUrlResolver,
        NftAvatarItemViewMapper viewMapper
    ) {
        return AdminNftItemDto.builder()
            .nftItemId(nftItem.getNftItemId())
            .tokenId(nftItem.getNftTokenId())
            .name(nftItem.getName())
            .category(nftItem.getCategory().name())
            .mainCategory(viewMapper.getMainCategory(nftItem))
            .subCategory(viewMapper.getSubCategory(nftItem))
            .rarity(nftItem.getRarity().name())
            .thumbnailStorageKey(nftItem.getThumbnailStorageKey())
            .thumbnailUrl(assetUrlResolver.resolve(nftItem.getThumbnailUrl(), nftItem.getThumbnailStorageKey()))
            .build();
    }
}
