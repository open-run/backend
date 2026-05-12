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
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class NftMintJobService {

    private final NftMintJobJpaRepository nftMintJobJpaRepository;
    private final UserChallengeJpaRepository userChallengeJpaRepository;
    private final NftMintJobProcessor nftMintJobProcessor;
    private final EthereumAddressValidator ethereumAddressValidator;
    private final TransactionTemplate transactionTemplate;

    public NftMintJobDto requestMintJob(UserDetailsImpl userDetails, Long userChallengeId) {
        String userId = userDetails.getUser().getUserId();

        PrepResult prep = transactionTemplate.execute(status -> prepareJob(userId, userChallengeId));

        if (prep.immediate() != null) {
            return prep.immediate();
        }

        // Phase 2: synchronous processing — markMinting → mintToken → markSuccess|markFailed
        // 각 단계는 NftMintJobProcessor 내부 TransactionTemplate 으로 자체 트랜잭션을 가진다.
        nftMintJobProcessor.process(prep.mintJobId());

        // Phase 3: 최종 상태(SUCCESS/FAILED 메타데이터 포함) 다시 읽어 DTO 반환
        NftMintJobEntity result = transactionTemplate.execute(status ->
            nftMintJobJpaRepository.findById(prep.mintJobId()).orElseThrow());
        return NftMintJobDto.from(result);
    }

    @Transactional(readOnly = true)
    public List<NftMintJobDto> getMyMintJobs(UserDetailsImpl userDetails) {
        return nftMintJobJpaRepository
            .findTop20ByUserEntityUserIdOrderByUpdatedAtDesc(userDetails.getUser().getUserId())
            .stream()
            .map(NftMintJobDto::from)
            .toList();
    }

    private PrepResult prepareJob(String userId, Long userChallengeId) {
        UserChallengeEntity userChallenge = userChallengeJpaRepository
            .findByUserChallengeId(userChallengeId)
            .orElseThrow(() -> new IllegalArgumentException("userChallenge not found"));

        if (!userChallenge.getUserEntity().getUserId().equals(userId)) {
            throw new AccessDeniedException("Cannot mint another user's challenge reward");
        }

        NftMintJobEntity existing = nftMintJobJpaRepository
            .findByUserChallengeEntityUserChallengeId(userChallengeId)
            .orElse(null);

        if (Boolean.TRUE.equals(userChallenge.getNftCompleted())) {
            // 정상 흐름에서는 markSuccess 가 동일 트랜잭션으로 mintJob.status=SUCCESS 와 nftCompleted=true 를 묶는다.
            // 이 분기에 도달했는데 mintJob 이 없거나 SUCCESS 가 아니면 데이터 불일치(수동 SQL/시드 등)이므로 즉시 실패시킨다.
            if (existing == null || !NftMintJobStatus.SUCCESS.equals(existing.getStatus())) {
                throw new IllegalStateException(
                    "Inconsistent reward state: userChallenge.nftCompleted=true but mintJob is "
                        + (existing == null ? "missing" : existing.getStatus())
                        + " for userChallengeId=" + userChallengeId);
            }
            return PrepResult.immediate(NftMintJobDto.from(existing));
        }

        validateRewardable(userChallenge);

        if (existing == null) {
            NftMintJobEntity saved = nftMintJobJpaRepository
                .save(new NftMintJobEntity(userChallenge.getUserEntity(), userChallenge));
            return PrepResult.toProcess(saved.getMintJobId());
        }
        if (NftMintJobStatus.FAILED.equals(existing.getStatus())) {
            existing.resetToPending();
            return PrepResult.toProcess(existing.getMintJobId());
        }
        // 동기 환경에서는 거의 발생하지 않으나 경합/leftover PENDING/MINTING 방어
        return PrepResult.immediate(NftMintJobDto.from(existing));
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

    private record PrepResult(NftMintJobDto immediate, Long mintJobId) {
        static PrepResult immediate(NftMintJobDto dto) {
            return new PrepResult(dto, null);
        }

        static PrepResult toProcess(Long id) {
            return new PrepResult(null, id);
        }
    }
}
