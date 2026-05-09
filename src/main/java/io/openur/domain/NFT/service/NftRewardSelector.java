package io.openur.domain.NFT.service;

import io.openur.domain.NFT.dto.NFTMetadataDto;
import io.openur.domain.NFT.entity.NftItemEntity;
import io.openur.domain.NFT.enums.NftItemCategory;
import io.openur.domain.NFT.enums.NftItemRarity;
import io.openur.domain.NFT.exception.MintException;
import io.openur.domain.NFT.repository.NftItemJpaRepository;
import io.openur.domain.challenge.entity.ChallengeEntity;
import io.openur.domain.challenge.entity.ChallengeStageEntity;
import io.openur.domain.userchallenge.entity.UserChallengeEntity;
import java.math.BigInteger;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NftRewardSelector {

    private final NftItemJpaRepository nftItemJpaRepository;
    private final NftAssetUrlResolver nftAssetUrlResolver;

    @Transactional(readOnly = true)
    public BigInteger selectTokenId(UserChallengeEntity userChallenge) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        ChallengeStageEntity stage = userChallenge.getChallengeStageEntity();
        ChallengeEntity challenge = stage.getChallengeEntity();

        NftItemCategory category = NftItemCategory.fromRewardType(challenge.getRewardType(), random);
        NftItemRarity rarity = pickRarity(stage, random);

        long candidateCount = nftItemJpaRepository
            .countByCategoryAndRarityAndEnabledTrueAndNftTokenIdIsNotNull(category, rarity);
        if (candidateCount == 0L) {
            throw new MintException(
                "No enabled NFT item for category=" + category + " rarity=" + rarity);
        }
        if (candidateCount > Integer.MAX_VALUE) {
            throw new MintException(
                "Candidate set too large to sample: count=" + candidateCount);
        }

        int offset = random.nextInt((int) candidateCount);
        Page<NftItemEntity> page = nftItemJpaRepository
            .findByCategoryAndRarityAndEnabledTrueAndNftTokenIdIsNotNullOrderByNftItemIdAsc(
                category, rarity, PageRequest.of(offset, 1));
        if (page.isEmpty()) {
            throw new MintException(
                "Candidate disappeared between count and fetch: category=" + category
                    + " rarity=" + rarity + " offset=" + offset);
        }

        NftItemEntity item = page.getContent().get(0);
        return new BigInteger(item.getNftTokenId());
    }

    @Transactional(readOnly = true)
    public NFTMetadataDto resolveMetadata(BigInteger tokenId) {
        NftItemEntity item = nftItemJpaRepository
            .findByNftTokenId(tokenId.toString())
            .orElseThrow(() -> new MintException("Unknown tokenId: " + tokenId));

        String image = nftAssetUrlResolver.resolve(item.getThumbnailUrl(), item.getThumbnailStorageKey());
        return NFTMetadataDto.builder()
            .tokenId(tokenId)
            .name(item.getName())
            .description(null)
            .image(image)
            .category(item.getCategory().name())
            .rarity(item.getRarity().name())
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
