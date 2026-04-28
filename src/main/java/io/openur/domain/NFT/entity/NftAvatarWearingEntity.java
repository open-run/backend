package io.openur.domain.NFT.entity;

import io.openur.domain.NFT.enums.NftAvatarWearingSlot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nft_item_id")
    private NftItemEntity nftItemEntity;
}
