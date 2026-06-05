package io.openur.domain.NFT.dto;

import io.openur.domain.NFT.entity.NftEntity;
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
    private String tokenId;
    private String balance;
    private String name;
    private String rarity;
    private String mainCategory;
    private String subCategory;
    private Object imageUrl;
    private String thumbnailUrl;

    public static NftAvatarItemDto from(
        NftEntity nft,
        String tokenId,
        BigInteger balance,
        String mainCategory,
        String subCategory,
        Object imageUrl,
        String thumbnailUrl
    ) {
        return NftAvatarItemDto.builder()
            .id(tokenId)
            .tokenId(tokenId)
            .balance(balance == null ? null : balance.toString())
            .name(nft.getName())
            .rarity(nft.getRarity().name())
            .mainCategory(mainCategory)
            .subCategory(subCategory)
            .imageUrl(imageUrl)
            .thumbnailUrl(thumbnailUrl)
            .build();
    }
}
