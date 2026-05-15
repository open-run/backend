package io.openur.domain.challenge.service;

import io.openur.domain.NFT.entity.NftMintJobEntity;
import io.openur.domain.NFT.enums.NftMintJobStatus;
import io.openur.domain.NFT.repository.NftMintJobJpaRepository;
import io.openur.domain.challenge.dto.CompletedChallengeWithNftDto;
import io.openur.domain.challenge.dto.GeneralChallengeDto;
import io.openur.domain.challenge.dto.RepetitiveChallengeTreeDto;
import io.openur.domain.challenge.exception.ChallengeNotAssignedException;
import io.openur.domain.challenge.exception.ChallengeStageInvalid;
import io.openur.domain.challenge.model.ChallengeStage;
import io.openur.domain.challenge.repository.ChallengeStageRepository;
import io.openur.domain.user.model.User;
import io.openur.domain.user.repository.UserRepository;
import io.openur.domain.userchallenge.model.UserChallenge;
import io.openur.domain.userchallenge.repository.UserChallengeRepository;
import io.openur.global.security.UserDetailsImpl;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeService {
    private final UserRepository userRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final ChallengeStageRepository challengeStageRepository;
    private final NftMintJobJpaRepository nftMintJobJpaRepository;

    public Page<GeneralChallengeDto> getGeneralChallengeList(
        UserDetailsImpl userDetails, Pageable pageable
    ) {
        User user = userRepository.findUser(userDetails.getUser());

        return userChallengeRepository.findUncompletedChallengesByUserId(
            user.getUserId(), pageable
        ).map(GeneralChallengeDto::new);
    }

    public Page<GeneralChallengeDto> getCompletedChallengeList(
        UserDetailsImpl userDetails, Pageable pageable
    ) {
        User user = userRepository.findUser(userDetails.getUser());

        return userChallengeRepository.findCompletedChallengesByUserId(
            user.getUserId(), pageable
        ).map(GeneralChallengeDto::new);
    }

    public Page<CompletedChallengeWithNftDto> getCompletedChallengesWithNft(
        UserDetailsImpl userDetails, Pageable pageable
    ) {
        User user = userRepository.findUser(userDetails.getUser());

        Page<UserChallenge> page = userChallengeRepository
            .findCompletedAndNftIssuedChallengesByUserId(user.getUserId(), pageable);

        if (page.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> userChallengeIds = page.getContent().stream()
            .map(UserChallenge::getUserChallengeId)
            .toList();

        Map<Long, NftMintJobEntity> jobsByUcId = nftMintJobJpaRepository
            .findByUserChallengeEntityUserChallengeIdInAndStatus(
                userChallengeIds, NftMintJobStatus.SUCCESS)
            .stream()
            .collect(Collectors.toMap(
                j -> j.getUserChallengeEntity().getUserChallengeId(),
                Function.identity()));

        return page.map(uc -> {
            NftMintJobEntity job = jobsByUcId.get(uc.getUserChallengeId());
            if (job == null) {
                throw new IllegalStateException(
                    "Inconsistent reward state: userChallenge " + uc.getUserChallengeId()
                        + " has nftCompleted=true but no SUCCESS mint job");
            }
            return CompletedChallengeWithNftDto.from(uc, job);
        });
    }

    public Page<GeneralChallengeDto> getRepetitiveChallengeList(
        UserDetailsImpl userDetails, Pageable pageable
    ) {
        User user = userRepository.findUser(userDetails.getUser());

        return userChallengeRepository.findRepetitiveChallengesByUserId(
            user.getUserId(), pageable
        ).map(GeneralChallengeDto::new);
    }

    public RepetitiveChallengeTreeDto getRepetitiveChallengeDetail(
        UserDetailsImpl userDetails, Long challengeId
    ) {
        User user = userRepository.findUser(userDetails.getUser());

        Map<Long, UserChallenge> userChallengeMap = userChallengeRepository
            .findRepetitiveUserChallengesMappedByStageId(
                user.getUserId(), challengeId
            );

        if(userChallengeMap.isEmpty())
            throw new ChallengeNotAssignedException("No challenge assigned to user");

        List<ChallengeStage> challengeStages = challengeStageRepository
            .findAllByChallengeId(challengeId);

        if(challengeStages.isEmpty())
            throw new ChallengeStageInvalid("No challenge stages found");

        return new RepetitiveChallengeTreeDto(userChallengeMap, challengeStages);
    }
}
