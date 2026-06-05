package io.openur.domain.NFT.entity;

import io.openur.domain.NFT.enums.NftImageRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Maps {@code tb_nft_tokens} (owned by {@code openrun-nft-tools}). Each catalog NFT
 * has up to three tokens — one per {@link NftImageRole} (thumbnail / avatar /
 * avatar_back) — since every image is minted as its own ERC-1155 token. The
 * {@code avatar} token is the wearable/ownership token used for balances and minting.
 * {@code tokenId} is a decimal string (up to 78 digits) and is permanently stable.
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name = "tb_nft_tokens",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_nft_role",
        columnNames = {"nft_id", "image_role"}
    )
)
public class NftTokenEntity {

    @Id
    @Column(name = "token_id")
    private String tokenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nft_id")
    private NftEntity nft;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_role")
    private NftImageRole imageRole;
}
