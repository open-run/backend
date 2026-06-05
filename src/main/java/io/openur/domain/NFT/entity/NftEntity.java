package io.openur.domain.NFT.entity;

import io.openur.domain.NFT.enums.NftItemCategory;
import io.openur.domain.NFT.enums.NftItemRarity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Maps the Swarm-backed NFT catalog table {@code tb_nfts}, owned and re-synced
 * (drop + recreate) by the {@code openrun-nft-tools} repo. {@code nftId} is NOT
 * stable across syncs and must never be persisted or exposed outside this table;
 * the stable identity is the {@code avatar} {@link NftTokenEntity#getTokenId()}.
 * Each {@code *_ref} is a complete, bare 64-char Swarm reference (no prefix, never
 * append a path).
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name = "tb_nfts",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_name_category",
        columnNames = {"name", "category"}
    )
)
public class NftEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nft_id")
    private Integer nftId;

    private String name;

    @Enumerated(EnumType.STRING)
    private NftItemCategory category;

    @Enumerated(EnumType.STRING)
    private NftItemRarity rarity;

    @Column(name = "thumbnail_ref")
    private String thumbnailRef;

    @Column(name = "avatar_ref")
    private String avatarRef;

    @Column(name = "avatar2_ref")
    private String avatar2Ref;
}
