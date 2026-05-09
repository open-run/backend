package io.openur.domain.NFT.enums;

import io.openur.domain.NFT.exception.MintException;
import io.openur.domain.challenge.enums.RewardType;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public enum NftItemCategory {
    top,
    pants,
    shoes,
    face,
    skin,
    hair,
    head_acc,
    ear_acc,
    body_acc;

    private static final List<NftItemCategory> ACCESSORY_CATEGORIES =
        List.of(head_acc, ear_acc, body_acc);

    public static NftItemCategory fromRewardType(RewardType type, ThreadLocalRandom random) {
        return switch (type) {
            case face -> face;
            case hair -> hair;
            case top -> top;
            case bottom -> pants;
            case footwear -> shoes;
            case skin -> skin;
            case accessory ->
                ACCESSORY_CATEGORIES.get(random.nextInt(ACCESSORY_CATEGORIES.size()));
            case pairs -> throw new MintException(
                "RewardType.pairs has no defined category mapping yet. "
                    + "Update NftItemCategory.fromRewardType when the semantic is decided.");
        };
    }
}
