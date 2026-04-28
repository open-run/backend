package io.openur.domain.NFT.dto;

import io.openur.domain.NFT.entity.NftItemEntity;
import io.openur.domain.NFT.service.NftAssetUrlResolver;
import java.math.BigInteger;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NftAvatarItemDto {

    private Long nftItemId;
    private String tokenId;
    private String balance;
    private String name;
    private String category;
    private String rarity;
    private String thumbnailStorageKey;
    private String thumbnailUrl;
    private List<NftAvatarItemEquipImageDto> equipImages;

    public static NftAvatarItemDto from(
        NftItemEntity nftItem,
        BigInteger balance,
        List<NftAvatarItemEquipImageDto> equipImages,
        NftAssetUrlResolver assetUrlResolver
    ) {
        String resolvedThumbnailUrl = assetUrlResolver.resolve(
            nftItem.getThumbnailUrl(),
            nftItem.getThumbnailStorageKey()
        );

        return NftAvatarItemDto.builder()
            .nftItemId(nftItem.getNftItemId())
            .tokenId(nftItem.getNftTokenId())
            .balance(balance.toString())
            .name(nftItem.getName())
            .category(nftItem.getCategory().name())
            .rarity(nftItem.getRarity().name())
            .thumbnailStorageKey(nftItem.getThumbnailStorageKey())
            .thumbnailUrl(resolvedThumbnailUrl)
            .equipImages(equipImages)
            .build();
    }
}
