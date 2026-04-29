package io.openur.domain.NFT.service;

import io.openur.domain.NFT.dto.NFTMetadataDto;
import io.openur.domain.NFT.entity.NftMintJobEntity;
import io.openur.domain.NFT.enums.NftMintJobStatus;
import io.openur.domain.NFT.repository.NftMintJobJpaRepository;
import io.openur.domain.userchallenge.repository.UserChallengeRepository;
import java.math.BigInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class NftMintJobProcessor {

    private static final BigInteger MINT_AMOUNT = BigInteger.ONE;

    private final NftMintJobJpaRepository nftMintJobJpaRepository;
    private final NftMintClient nftMintClient;
    private final NftRewardMetadataResolver nftRewardMetadataResolver;
    private final UserChallengeRepository userChallengeRepository;
    private final TransactionTemplate transactionTemplate;

    public void process(Long mintJobId) {
        MintContext mintContext = markMinting(mintJobId);
        if (mintContext == null) {
            return;
        }

        try {
            String transactionHash = nftMintClient.mintToken(
                mintContext.recipientAddress(),
                mintContext.tokenId(),
                MINT_AMOUNT
            );
            NFTMetadataDto metadata = nftRewardMetadataResolver.getMetadata(mintContext.tokenId());
            markSuccess(mintJobId, mintContext.userChallengeId(), transactionHash, metadata);
        } catch (Exception e) {
            markFailed(mintJobId, e.getMessage());
        }
    }

    private MintContext markMinting(Long mintJobId) {
        return transactionTemplate.execute(status -> {
            NftMintJobEntity mintJob = nftMintJobJpaRepository.findById(mintJobId).orElse(null);
            if (mintJob == null || !NftMintJobStatus.PENDING.equals(mintJob.getStatus())) {
                return null;
            }

            BigInteger tokenId = nftRewardMetadataResolver.getRewardTokenId();
            mintJob.markMinting(tokenId.toString());

            return new MintContext(
                mintJob.getUserEntity().getBlockchainAddress(),
                mintJob.getUserChallengeEntity().getUserChallengeId(),
                tokenId
            );
        });
    }

    private void markSuccess(
        Long mintJobId,
        Long userChallengeId,
        String transactionHash,
        NFTMetadataDto metadata
    ) {
        transactionTemplate.executeWithoutResult(status -> {
            NftMintJobEntity mintJob = nftMintJobJpaRepository.findById(mintJobId).orElseThrow();
            mintJob.markSuccess(transactionHash, metadata);
            userChallengeRepository.markNftCompleted(userChallengeId);
        });
    }

    private void markFailed(Long mintJobId, String errorMessage) {
        transactionTemplate.executeWithoutResult(status -> {
            NftMintJobEntity mintJob = nftMintJobJpaRepository.findById(mintJobId).orElseThrow();
            mintJob.markFailed(errorMessage);
        });
    }

    private record MintContext(String recipientAddress, Long userChallengeId, BigInteger tokenId) {
    }
}
