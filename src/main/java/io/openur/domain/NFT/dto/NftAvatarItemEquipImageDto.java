package io.openur.domain.NFT.dto;

import io.openur.domain.NFT.entity.NftItemEquipImageEntity;
import io.openur.domain.NFT.service.NftAssetUrlResolver;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NftAvatarItemEquipImageDto {

    private Long nftItemEquipImageId;
    private String equipPosition;
    private String storageKey;
    private String imageUrl;
    private Integer sortOrder;

    public static NftAvatarItemEquipImageDto from(
        NftItemEquipImageEntity equipImage,
        NftAssetUrlResolver assetUrlResolver
    ) {
        String resolvedImageUrl = assetUrlResolver.resolve(
            equipImage.getImageUrl(),
            equipImage.getStorageKey()
        );

        return NftAvatarItemEquipImageDto.builder()
            .nftItemEquipImageId(equipImage.getNftItemEquipImageId())
            .equipPosition(equipImage.getEquipPosition().name())
            .storageKey(equipImage.getStorageKey())
            .imageUrl(resolvedImageUrl)
            .sortOrder(equipImage.getSortOrder())
            .build();
    }
}
