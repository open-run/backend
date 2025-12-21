package io.openur.domain.NFT.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NFTMetadataDto {
    private String name;
    private String description;
    private String image;
    private String category;
    private String rarity;
}
