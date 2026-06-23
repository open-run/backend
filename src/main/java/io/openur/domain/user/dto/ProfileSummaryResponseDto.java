package io.openur.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.openur.domain.NFT.entity.NftMintJobEntity;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;

@Getter
public class ProfileSummaryResponseDto {

    private final long receivedLikeCount;
    private final long currentOwnedBungCount;
    private final long acquiredNftCount;
    private final List<RecentAcquiredNftDto> recentAcquiredNfts;

    public ProfileSummaryResponseDto(
        long receivedLikeCount,
        long currentOwnedBungCount,
        long acquiredNftCount,
        List<NftMintJobEntity> recentAcquiredNftJobs
    ) {
        this.receivedLikeCount = receivedLikeCount;
        this.currentOwnedBungCount = currentOwnedBungCount;
        this.acquiredNftCount = acquiredNftCount;
        this.recentAcquiredNfts = recentAcquiredNftJobs.stream()
            .map(RecentAcquiredNftDto::new)
            .toList();
    }

    @Getter
    public static class RecentAcquiredNftDto {
        private final Long userChallengeId;
        private final Long challengeId;
        private final String challengeName;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private final LocalDateTime acquiredAt;
        private final NftInfoDto nft;

        private RecentAcquiredNftDto(NftMintJobEntity mintJob) {
            var userChallenge = mintJob.getUserChallengeEntity();
            var challenge = userChallenge
                .getChallengeStageEntity()
                .getChallengeEntity();

            this.userChallengeId = userChallenge.getUserChallengeId();
            this.challengeId = challenge.getChallengeId();
            this.challengeName = challenge.getName();
            this.acquiredAt = mintJob.getUpdatedAt();
            this.nft = new NftInfoDto(mintJob);
        }
    }

    @Getter
    public static class NftInfoDto {
        private final String tokenId;
        private final String transactionHash;
        private final String name;
        private final String description;
        private final String image;
        private final String category;
        private final String rarity;

        private NftInfoDto(NftMintJobEntity mintJob) {
            this.tokenId = mintJob.getTokenId();
            this.transactionHash = mintJob.getTransactionHash();
            this.name = mintJob.getNftName();
            this.description = mintJob.getNftDescription();
            this.image = mintJob.getNftImage();
            this.category = mintJob.getNftCategory();
            this.rarity = mintJob.getNftRarity();
        }
    }
}
