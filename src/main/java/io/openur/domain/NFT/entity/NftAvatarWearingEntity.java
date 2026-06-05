package io.openur.domain.NFT.entity;

import io.openur.domain.NFT.enums.NftAvatarWearingSlot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * User wearing state. Keyed on the stable avatar {@code token_id} (NOT the volatile
 * {@code tb_nfts.nft_id}), so wearing rows survive catalog re-syncs by the
 * openrun-nft-tools. No FK into the tool-owned catalog tables.
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(NftAvatarWearingEntityId.class)
@Table(name = "tb_user_nft_avatar_wearing")
public class NftAvatarWearingEntity {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "wearing_slot")
    private NftAvatarWearingSlot wearingSlot;

    @Column(name = "token_id")
    private String tokenId;
}
