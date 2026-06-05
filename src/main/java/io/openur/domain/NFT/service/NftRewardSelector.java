package io.openur.domain.NFT.service;

import io.openur.domain.NFT.dto.NFTMetadataDto;
import io.openur.domain.NFT.entity.NftEntity;
import io.openur.domain.NFT.entity.NftTokenEntity;
import io.openur.domain.NFT.enums.NftImageRole;
import io.openur.domain.NFT.enums.NftItemCategory;
import io.openur.domain.NFT.enums.NftItemRarity;
import io.openur.domain.NFT.exception.MintException;
import io.openur.domain.NFT.repository.NftTokenJpaRepository;
import io.openur.domain.challenge.entity.ChallengeEntity;
import io.openur.domain.challenge.entity.ChallengeStageEntity;
import io.openur.domain.userchallenge.entity.UserChallengeEntity;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NftRewardSelector {

    private final NftTokenJpaRepository nftTokenJpaRepository;
    private final NftAssetUrlResolver nftAssetUrlResolver;

    @Transactional(readOnly = true)
    public BigInteger selectTokenId(UserChallengeEntity userChallenge) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        ChallengeStageEntity stage = userChallenge.getChallengeStageEntity();
        ChallengeEntity challenge = stage.getChallengeEntity();

        NftItemCategory category = NftItemCategory.fromRewardType(challenge.getRewardType(), random);
        NftItemRarity rarity = pickRarity(stage, random);

        long candidateCount = nftTokenJpaRepository
            .countByImageRoleAndNftCategoryAndNftRarity(NftImageRole.avatar, category, rarity);
        if (candidateCount == 0L) {
            throw new MintException(
                "No available NFT (no avatar token) for category=" + category + " rarity=" + rarity);
        }
        if (candidateCount > Integer.MAX_VALUE) {
            throw new MintException(
                "Candidate set too large to sample: count=" + candidateCount);
        }

        int offset = random.nextInt((int) candidateCount);
        List<NftTokenEntity> candidates = nftTokenJpaRepository
            .findByImageRoleAndNftCategoryAndNftRarityOrderByNftNftIdAsc(
                NftImageRole.avatar, category, rarity, PageRequest.of(offset, 1));
        if (candidates.isEmpty()) {
            throw new MintException(
                "Candidate disappeared between count and fetch: category=" + category
                    + " rarity=" + rarity + " offset=" + offset);
        }

        return new BigInteger(candidates.get(0).getTokenId());
    }

    @Transactional(readOnly = true)
    public NFTMetadataDto resolveMetadata(BigInteger tokenId) {
        NftTokenEntity token = nftTokenJpaRepository
            .findByTokenIdAndImageRole(tokenId.toString(), NftImageRole.avatar)
            .orElseThrow(() -> new MintException("Unknown tokenId: " + tokenId));
        NftEntity nft = token.getNft();

        String image = nftAssetUrlResolver.resolve(nft.getThumbnailRef());
        return NFTMetadataDto.builder()
            .tokenId(tokenId)
            .name(nft.getName())
            .description(null)
            .image(image)
            .category(nft.getCategory().name())
            .rarity(nft.getRarity().name())
            .build();
    }

    private NftItemRarity pickRarity(ChallengeStageEntity stage, ThreadLocalRandom random) {
        int common = stage.getWeightCommon();
        int rare = stage.getWeightRare();
        int epic = stage.getWeightEpic();
        int total = common + rare + epic;
        if (total <= 0) {
            return NftItemRarity.common;
        }

        int roll = random.nextInt(total);
        if (roll < common) {
            return NftItemRarity.common;
        }
        if (roll < common + rare) {
            return NftItemRarity.rare;
        }
        return NftItemRarity.epic;
    }
}
