package io.openur.domain.NFT.service;

import io.openur.domain.NFT.dto.NftMintJobDto;
import io.openur.domain.NFT.entity.NftMintJobEntity;
import io.openur.domain.NFT.enums.NftMintJobStatus;
import io.openur.domain.NFT.repository.NftMintJobJpaRepository;
import io.openur.domain.userchallenge.entity.UserChallengeEntity;
import io.openur.domain.userchallenge.repository.UserChallengeJpaRepository;
import io.openur.global.common.validation.EthereumAddressValidator;
import io.openur.global.security.UserDetailsImpl;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class NftMintJobService {

    private final NftMintJobJpaRepository nftMintJobJpaRepository;
    private final UserChallengeJpaRepository userChallengeJpaRepository;
    private final NftMintJobAsyncExecutor nftMintJobAsyncExecutor;
    private final EthereumAddressValidator ethereumAddressValidator;

    @Transactional
    public NftMintJobDto requestMintJob(UserDetailsImpl userDetails, Long userChallengeId) {
        String userId = userDetails.getUser().getUserId();
        UserChallengeEntity userChallenge = userChallengeJpaRepository
            .findByUserChallengeId(userChallengeId)
            .orElseThrow(() -> new IllegalArgumentException("userChallenge not found"));

        if (!userChallenge.getUserEntity().getUserId().equals(userId)) {
            throw new AccessDeniedException("Cannot mint another user's challenge reward");
        }

        NftMintJobEntity mintJob = nftMintJobJpaRepository
            .findByUserChallengeEntityUserChallengeId(userChallengeId)
            .orElse(null);

        if (Boolean.TRUE.equals(userChallenge.getNftCompleted())) {
            // 정상 흐름에서는 markSuccess 가 동일 트랜잭션으로 mintJob.status=SUCCESS 와 nftCompleted=true 를 묶는다.
            // 이 분기에 도달했는데 mintJob 이 없거나 SUCCESS 가 아니면 데이터 불일치(수동 SQL/시드 등)이므로 즉시 실패시킨다.
            if (mintJob == null || !NftMintJobStatus.SUCCESS.equals(mintJob.getStatus())) {
                throw new IllegalStateException(
                    "Inconsistent reward state: userChallenge.nftCompleted=true but mintJob is "
                        + (mintJob == null ? "missing" : mintJob.getStatus())
                        + " for userChallengeId=" + userChallengeId);
            }
            return NftMintJobDto.from(mintJob);
        }

        validateRewardable(userChallenge);

        boolean shouldDispatch = false;
        if (mintJob == null) {
            mintJob = nftMintJobJpaRepository.save(new NftMintJobEntity(userChallenge.getUserEntity(), userChallenge));
            shouldDispatch = true;
        } else if (NftMintJobStatus.FAILED.equals(mintJob.getStatus())) {
            mintJob.resetToPending();
            shouldDispatch = true;
        }

        if (shouldDispatch) {
            dispatchAfterCommit(mintJob.getMintJobId());
        }

        return NftMintJobDto.from(mintJob);
    }

    @Transactional(readOnly = true)
    public List<NftMintJobDto> getMyMintJobs(UserDetailsImpl userDetails) {
        return nftMintJobJpaRepository
            .findTop20ByUserEntityUserIdOrderByUpdatedAtDesc(userDetails.getUser().getUserId())
            .stream()
            .map(NftMintJobDto::from)
            .toList();
    }

    private void validateRewardable(UserChallengeEntity userChallenge) {
        if (userChallenge.getCompletedDate() == null) {
            throw new IllegalArgumentException("challenge reward is not issuable yet");
        }

        int currentCount = userChallenge.getCurrentCount();
        int conditionCount = userChallenge.getChallengeStageEntity().getConditionAsCount();
        if (currentCount < conditionCount) {
            throw new IllegalArgumentException("challenge reward is not issuable yet");
        }

        String walletAddress = userChallenge.getUserEntity().getBlockchainAddress();
        if (!ethereumAddressValidator.isValid(walletAddress)) {
            throw new IllegalArgumentException(
                "User wallet address is invalid; cannot mint reward");
        }
    }

    private void dispatchAfterCommit(Long mintJobId) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            nftMintJobAsyncExecutor.processAsync(mintJobId);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                nftMintJobAsyncExecutor.processAsync(mintJobId);
            }
        });
    }
}
