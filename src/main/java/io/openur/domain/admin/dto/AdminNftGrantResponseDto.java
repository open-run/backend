package io.openur.domain.admin.dto;

import io.openur.domain.NFT.entity.NftItemEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminNftGrantResponseDto {

    private String recipientAddress;
    private Long nftItemId;
    private String tokenId;
    private String transactionHash;

    public static AdminNftGrantResponseDto from(
        String recipientAddress,
        NftItemEntity nftItem,
        String transactionHash
    ) {
        return AdminNftGrantResponseDto.builder()
            .recipientAddress(recipientAddress)
            .nftItemId(nftItem.getNftItemId())
            .tokenId(nftItem.getNftTokenId())
            .transactionHash(transactionHash)
            .build();
    }
}
