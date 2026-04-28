package io.openur.domain.NFT.dto;

import io.openur.domain.NFT.entity.NftItemEntity;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NftAvatarItemDto {

    private String id;
    private Long nftItemId;
    private String tokenId;
    private String balance;
    private String name;
    private String rarity;
    private String mainCategory;
    private String subCategory;
    private Object imageUrl;
    private String storageKey;
    private String thumbnailStorageKey;
    private String thumbnailUrl;

    public static NftAvatarItemDto from(
        NftItemEntity nftItem,
        BigInteger balance,
        String mainCategory,
        String subCategory,
        Object imageUrl,
        String storageKey,
        String thumbnailUrl
    ) {
        return NftAvatarItemDto.builder()
            .id(nftItem.getNftItemId().toString())
            .nftItemId(nftItem.getNftItemId())
            .tokenId(nftItem.getNftTokenId())
            .balance(balance == null ? null : balance.toString())
            .name(nftItem.getName())
            .rarity(nftItem.getRarity().name())
            .mainCategory(mainCategory)
            .subCategory(subCategory)
            .imageUrl(imageUrl)
            .storageKey(storageKey)
            .thumbnailStorageKey(nftItem.getThumbnailStorageKey())
            .thumbnailUrl(thumbnailUrl)
            .build();
    }
}
