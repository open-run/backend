package io.openur.domain.NFT.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.openur.domain.NFT.entity.NftMintJobEntity;
import io.openur.domain.NFT.enums.NftMintJobStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NftMintJobDto {

    private Long mintJobId;
    private Long userChallengeId;
    private String challengeName;
    private NftMintJobStatus status;
    private String tokenId;
    private String transactionHash;
    private String errorMessage;
    private String nftName;
    private String nftDescription;
    private String nftImage;
    private String nftCategory;
    private String nftRarity;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public static NftMintJobDto from(NftMintJobEntity entity) {
        return NftMintJobDto.builder()
            .mintJobId(entity.getMintJobId())
            .userChallengeId(entity.getUserChallengeEntity().getUserChallengeId())
            .challengeName(entity.getUserChallengeEntity().getChallengeStageEntity().getChallengeEntity().getName())
            .status(entity.getStatus())
            .tokenId(entity.getTokenId())
            .transactionHash(entity.getTransactionHash())
            .errorMessage(entity.getErrorMessage())
            .nftName(entity.getNftName())
            .nftDescription(entity.getNftDescription())
            .nftImage(entity.getNftImage())
            .nftCategory(entity.getNftCategory())
            .nftRarity(entity.getNftRarity())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}
