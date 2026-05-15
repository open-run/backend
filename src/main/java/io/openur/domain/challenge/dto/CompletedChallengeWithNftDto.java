package io.openur.domain.challenge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.openur.domain.NFT.entity.NftMintJobEntity;
import io.openur.domain.challenge.enums.ChallengeType;
import io.openur.domain.challenge.enums.CompletedType;
import io.openur.domain.challenge.model.Challenge;
import io.openur.domain.challenge.model.ChallengeStage;
import io.openur.domain.userchallenge.model.UserChallenge;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CompletedChallengeWithNftDto {

    private final Long challengeId;
    private final Long userChallengeId;
    private final String challengeName;
    private final String challengeDescription;
    private final LocalDateTime completedDate;
    private final ChallengeType challengeType;
    private final CompletedType completedType;
    private final Integer stageCount;
    private final Integer currentCount;
    private final Integer conditionCount;
    private final NftInfo nft;

    private CompletedChallengeWithNftDto(UserChallenge userChallenge, NftMintJobEntity mintJob) {
        ChallengeStage challengeStage = userChallenge.getChallengeStage();
        Challenge challenge = challengeStage.getChallenge();

        this.userChallengeId = userChallenge.getUserChallengeId();
        this.completedDate = userChallenge.getCompletedDate();
        this.currentCount = userChallenge.getCurrentCount();

        this.stageCount = challengeStage.getStageNumber();
        this.conditionCount = challengeStage.getConditionAsCount();

        this.challengeId = challenge.getChallengeId();
        this.challengeName = challenge.getChallengeName();
        this.challengeDescription = challenge.getChallengeDescription();
        this.challengeType = challenge.getChallengeType();
        this.completedType = challenge.getCompletedType();

        this.nft = NftInfo.from(mintJob);
    }

    public static CompletedChallengeWithNftDto from(UserChallenge userChallenge,
        NftMintJobEntity mintJob) {
        return new CompletedChallengeWithNftDto(userChallenge, mintJob);
    }

    @Getter
    @Builder
    public static class NftInfo {

        private final String tokenId;
        private final String transactionHash;
        private final String name;
        private final String description;
        private final String image;
        private final String category;
        private final String rarity;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private final LocalDateTime mintedAt;

        static NftInfo from(NftMintJobEntity mintJob) {
            return NftInfo.builder()
                .tokenId(mintJob.getTokenId())
                .transactionHash(mintJob.getTransactionHash())
                .name(mintJob.getNftName())
                .description(mintJob.getNftDescription())
                .image(mintJob.getNftImage())
                .category(mintJob.getNftCategory())
                .rarity(mintJob.getNftRarity())
                .mintedAt(mintJob.getUpdatedAt())
                .build();
        }
    }
}
