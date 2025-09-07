package io.openur.domain.userchallenge.repository;

import io.openur.domain.challenge.model.CompletedType;
import io.openur.domain.userchallenge.dto.UserChallengeInfoDto;
import io.openur.domain.userchallenge.model.UserChallenge;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserChallengeRepository {

    UserChallenge save(UserChallenge userChallenge);

    /**
     * Increments the currentCount for challenges of type COUNT
     * @param userChallengeIds List of user challenge IDs to update
     */
    void bulkIncrementCount(List<Long> userChallengeIds);

    /**
     * Updates completion status after NFT airdrop
     * @param completedChallenges List of challenges that have been completed and had NFT airdropped
     */
    void bulkUpdateCompletedChallenges(List<Long> completedUserChallengeIds);

    List<UserChallenge> findByUserId(String userId);

    Page<UserChallenge> findByUserIdAndChallengeType(
        String userId, Pageable pageable
    );

    Page<UserChallenge> findCompletedChallengesByUserId(
        String userId, Pageable pageable
    );

    Page<UserChallengeInfoDto> findAllByUserId(String string,
        Pageable pageable);
    
    List<UserChallenge> findByUserIdsAndChallengeIds(List<String> userIds, List<Long> challengeIds);

    Map<CompletedType, List<UserChallenge>> findByUserIdsAndChallengeIdsGroupByCompletedType(
        List<String> userIds, List<Long> challengeIds
    );

    Optional<UserChallenge> findOptionalByUserIdAndChallengeId(String userId, Long challengeId);

    boolean existsByUserIdAndChallengeId(String userId, Long challengeId);

    void delete(UserChallenge userChallenge);
} 
