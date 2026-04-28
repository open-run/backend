package io.openur.domain.NFT.entity;

import io.openur.domain.NFT.enums.NftAvatarWearingSlot;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class NftAvatarWearingEntityId implements Serializable {

    private String userId;
    private NftAvatarWearingSlot wearingSlot;
}
