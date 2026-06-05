package io.openur.domain.NFT.service;

import io.openur.domain.NFT.dto.NftAvatarItemDto;
import io.openur.domain.NFT.entity.NftEntity;
import io.openur.domain.NFT.enums.NftAvatarWearingSlot;
import io.openur.domain.NFT.enums.NftItemCategory;
import java.math.BigInteger;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class NftAvatarItemViewMapper {

    private final NftAssetUrlResolver nftAssetUrlResolver;

    public NftAvatarItemDto toDto(NftEntity nft, String avatarTokenId, BigInteger balance) {
        return NftAvatarItemDto.from(
            nft,
            avatarTokenId,
            balance,
            getMainCategory(nft),
            getSubCategory(nft),
            getImageUrl(nft),
            nftAssetUrlResolver.resolve(nft.getThumbnailRef())
        );
    }

    public boolean matchesSlot(NftAvatarWearingSlot slot, NftEntity nft) {
        return switch (slot) {
            case upper_clothing -> nft.getCategory() == NftItemCategory.top;
            case lower_clothing -> nft.getCategory() == NftItemCategory.pants;
            case footwear -> nft.getCategory() == NftItemCategory.shoes;
            case face -> nft.getCategory() == NftItemCategory.face;
            case skin -> nft.getCategory() == NftItemCategory.skin;
            case hair -> nft.getCategory() == NftItemCategory.hair;
            case head_accessories -> nft.getCategory() == NftItemCategory.head_acc && !isEyeAccessory(nft);
            case eye_accessories -> nft.getCategory() == NftItemCategory.head_acc && isEyeAccessory(nft);
            case ear_accessories -> nft.getCategory() == NftItemCategory.ear_acc;
            case body_accessories -> nft.getCategory() == NftItemCategory.body_acc;
        };
    }

    public String getMainCategory(NftEntity nft) {
        return switch (nft.getCategory()) {
            case top -> "upperClothing";
            case pants -> "lowerClothing";
            case shoes -> "footwear";
            case face -> "face";
            case skin -> "skin";
            case hair -> "hair";
            case head_acc, ear_acc, body_acc -> "accessories";
        };
    }

    public String getSubCategory(NftEntity nft) {
        return switch (nft.getCategory()) {
            case head_acc -> isEyeAccessory(nft) ? "eye-accessories" : "head-accessories";
            case ear_acc -> "ear-accessories";
            case body_acc -> "body-accessories";
            default -> null;
        };
    }

    private Object getImageUrl(NftEntity nft) {
        if (nft.getCategory() == NftItemCategory.hair) {
            return getHairImageUrls(nft);
        }

        return nftAssetUrlResolver.resolve(nft.getAvatarRef());
    }

    private List<String> getHairImageUrls(NftEntity nft) {
        List<String> imageUrls = new ArrayList<>();
        String front = nftAssetUrlResolver.resolve(nft.getAvatarRef());
        String back = nftAssetUrlResolver.resolve(nft.getAvatar2Ref());
        if (StringUtils.hasText(front)) {
            imageUrls.add(front);
        }
        if (StringUtils.hasText(back)) {
            imageUrls.add(back);
        }
        return imageUrls;
    }

    private boolean isEyeAccessory(NftEntity nft) {
        String name = nft.getName();
        if (name == null) {
            return false;
        }

        // Catalog names may be NFD-encoded (macOS source); normalize before comparing to NFC literals.
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFC);
        return normalized.contains("안경") || normalized.contains("선글라스");
    }
}
