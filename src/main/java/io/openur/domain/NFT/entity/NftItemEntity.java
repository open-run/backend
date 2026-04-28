package io.openur.domain.NFT.entity;

import io.openur.domain.NFT.enums.NftItemCategory;
import io.openur.domain.NFT.enums.NftItemRarity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name = "tb_nft_items",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_nft_items_name_category",
        columnNames = {"name", "category"}
    )
)
public class NftItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nft_item_id")
    private Long nftItemId;

    private String name;

    @Enumerated(EnumType.STRING)
    private NftItemCategory category;

    @Enumerated(EnumType.STRING)
    private NftItemRarity rarity;

    @Column(name = "nft_token_id")
    private String nftTokenId;

    @Column(name = "thumbnail_storage_key")
    private String thumbnailStorageKey;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    private Boolean enabled;

    @OneToMany(mappedBy = "nftItemEntity", fetch = FetchType.LAZY)
    private List<NftItemEquipImageEntity> equipImages;
}
