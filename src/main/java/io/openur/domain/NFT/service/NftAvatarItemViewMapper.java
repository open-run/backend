package io.openur.domain.NFT.service;

import io.openur.domain.NFT.dto.NftAvatarItemDto;
import io.openur.domain.NFT.entity.NftItemEntity;
import io.openur.domain.NFT.entity.NftItemEquipImageEntity;
import io.openur.domain.NFT.enums.NftAvatarWearingSlot;
import io.openur.domain.NFT.enums.NftItemCategory;
import io.openur.domain.NFT.enums.NftItemEquipPosition;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NftAvatarItemViewMapper {

    private final NftAssetUrlResolver nftAssetUrlResolver;

    public NftAvatarItemDto toDto(
        NftItemEntity nftItem,
        BigInteger balance,
        List<NftItemEquipImageEntity> equipImages
    ) {
        String thumbnailUrl = nftAssetUrlResolver.resolve(
            nftItem.getThumbnailUrl(),
            nftItem.getThumbnailStorageKey()
        );
        String storageKey = getPrimaryStorageKey(nftItem, equipImages);

        return NftAvatarItemDto.from(
            nftItem,
            balance,
            getMainCategory(nftItem),
            getSubCategory(nftItem),
            getImageUrl(nftItem, equipImages),
            storageKey,
            thumbnailUrl
        );
    }

    public boolean matchesSlot(NftAvatarWearingSlot slot, NftItemEntity nftItem) {
        return switch (slot) {
            case upper_clothing -> nftItem.getCategory() == NftItemCategory.top;
            case lower_clothing -> nftItem.getCategory() == NftItemCategory.pants;
            case footwear -> nftItem.getCategory() == NftItemCategory.shoes;
            case face -> nftItem.getCategory() == NftItemCategory.face;
            case skin -> nftItem.getCategory() == NftItemCategory.skin;
            case hair -> nftItem.getCategory() == NftItemCategory.hair;
            case head_accessories -> nftItem.getCategory() == NftItemCategory.head_acc && !isEyeAccessory(nftItem);
            case eye_accessories -> nftItem.getCategory() == NftItemCategory.head_acc && isEyeAccessory(nftItem);
            case ear_accessories -> nftItem.getCategory() == NftItemCategory.ear_acc;
            case body_accessories -> nftItem.getCategory() == NftItemCategory.body_acc;
        };
    }

    public String getMainCategory(NftItemEntity nftItem) {
        return switch (nftItem.getCategory()) {
            case top -> "upperClothing";
            case pants -> "lowerClothing";
            case shoes -> "footwear";
            case face -> "face";
            case skin -> "skin";
            case hair -> "hair";
            case head_acc, ear_acc, body_acc -> "accessories";
        };
    }

    public String getSubCategory(NftItemEntity nftItem) {
        return switch (nftItem.getCategory()) {
            case head_acc -> isEyeAccessory(nftItem) ? "eye-accessories" : "head-accessories";
            case ear_acc -> "ear-accessories";
            case body_acc -> "body-accessories";
            default -> null;
        };
    }

    private Object getImageUrl(NftItemEntity nftItem, List<NftItemEquipImageEntity> equipImages) {
        if (nftItem.getCategory() == NftItemCategory.hair) {
            return getHairImageUrls(equipImages);
        }

        return getEquipImageByPosition(equipImages, NftItemEquipPosition.single)
            .map(this::resolveImageUrl)
            .orElse(null);
    }

    private List<String> getHairImageUrls(List<NftItemEquipImageEntity> equipImages) {
        return equipImages.stream()
            .filter(equipImage -> equipImage.getEquipPosition() == NftItemEquipPosition.front
                || equipImage.getEquipPosition() == NftItemEquipPosition.back)
            .sorted(Comparator.comparingInt(equipImage ->
                equipImage.getEquipPosition() == NftItemEquipPosition.front ? 0 : 1
            ))
            .map(this::resolveImageUrl)
            .filter(imageUrl -> imageUrl != null && !imageUrl.isBlank())
            .toList();
    }

    private String getPrimaryStorageKey(NftItemEntity nftItem, List<NftItemEquipImageEntity> equipImages) {
        if (nftItem.getCategory() == NftItemCategory.hair) {
            return getEquipImageByPosition(equipImages, NftItemEquipPosition.front)
                .or(() -> getEquipImageByPosition(equipImages, NftItemEquipPosition.back))
                .map(NftItemEquipImageEntity::getStorageKey)
                .orElse(null);
        }

        return getEquipImageByPosition(equipImages, NftItemEquipPosition.single)
            .map(NftItemEquipImageEntity::getStorageKey)
            .orElse(null);
    }

    private Optional<NftItemEquipImageEntity> getEquipImageByPosition(
        List<NftItemEquipImageEntity> equipImages,
        NftItemEquipPosition position
    ) {
        return equipImages.stream()
            .filter(equipImage -> equipImage.getEquipPosition() == position)
            .findFirst();
    }

    private String resolveImageUrl(NftItemEquipImageEntity equipImage) {
        return nftAssetUrlResolver.resolve(equipImage.getImageUrl(), equipImage.getStorageKey());
    }

    private boolean isEyeAccessory(NftItemEntity nftItem) {
        String name = nftItem.getName();
        return name != null && (name.contains("안경") || name.contains("선글라스"));
    }
}
