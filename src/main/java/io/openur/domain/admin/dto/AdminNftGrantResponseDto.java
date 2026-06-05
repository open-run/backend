package io.openur.domain.admin.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminNftGrantResponseDto {

    private String recipientAddress;
    private String tokenId;
    private String transactionHash;

    public static AdminNftGrantResponseDto from(
        String recipientAddress,
        String tokenId,
        String transactionHash
    ) {
        return AdminNftGrantResponseDto.builder()
            .recipientAddress(recipientAddress)
            .tokenId(tokenId)
            .transactionHash(transactionHash)
            .build();
    }
}
