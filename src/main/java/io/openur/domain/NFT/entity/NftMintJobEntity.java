package io.openur.domain.NFT.entity;

import io.openur.domain.NFT.dto.NFTMetadataDto;
import io.openur.domain.NFT.enums.NftMintJobStatus;
import io.openur.domain.user.entity.UserEntity;
import io.openur.domain.userchallenge.entity.UserChallengeEntity;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "tb_nft_mint_jobs")
@NoArgsConstructor
public class NftMintJobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mintJobId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity userEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_challenge_id", nullable = false)
    private UserChallengeEntity userChallengeEntity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NftMintJobStatus status;

    private String tokenId;

    private String transactionHash;

    @Column(length = 1024)
    private String errorMessage;

    private String nftName;

    @Column(length = 1024)
    private String nftDescription;

    @Column(length = 1024)
    private String nftImage;

    private String nftCategory;

    private String nftRarity;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public NftMintJobEntity(UserEntity userEntity, UserChallengeEntity userChallengeEntity) {
        this.userEntity = userEntity;
        this.userChallengeEntity = userChallengeEntity;
        this.status = NftMintJobStatus.PENDING;
    }

    public void resetToPending() {
        this.status = NftMintJobStatus.PENDING;
        this.transactionHash = null;
        this.errorMessage = null;
    }

    public void markMinting(String tokenId) {
        this.status = NftMintJobStatus.MINTING;
        this.tokenId = tokenId;
        this.errorMessage = null;
    }

    public void markSuccess(String transactionHash, NFTMetadataDto metadata) {
        this.status = NftMintJobStatus.SUCCESS;
        this.transactionHash = transactionHash;
        this.errorMessage = null;
        if (metadata != null) {
            this.tokenId = metadata.getTokenId().toString();
            this.nftName = metadata.getName();
            this.nftDescription = metadata.getDescription();
            this.nftImage = metadata.getImage();
            this.nftCategory = metadata.getCategory();
            this.nftRarity = metadata.getRarity();
        }
    }

    public void markFailed(String errorMessage) {
        this.status = NftMintJobStatus.FAILED;
        this.errorMessage = truncateErrorMessage(errorMessage);
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    private String truncateErrorMessage(String errorMessage) {
        if (errorMessage == null || errorMessage.length() <= 1024) {
            return errorMessage;
        }

        return errorMessage.substring(0, 1024);
    }
}
