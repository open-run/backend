package io.openur.domain.NFT.entity;

import io.openur.domain.NFT.enums.NftItemEquipPosition;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name = "tb_nft_item_equip_images",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_nft_item_equip_position",
        columnNames = {"nft_item_id", "equip_position"}
    )
)
public class NftItemEquipImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nft_item_equip_image_id")
    private Long nftItemEquipImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nft_item_id")
    private NftItemEntity nftItemEntity;

    @Enumerated(EnumType.STRING)
    @Column(name = "equip_position")
    private NftItemEquipPosition equipPosition;

    @Column(name = "storage_key")
    private String storageKey;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "sort_order")
    private Integer sortOrder;
}
