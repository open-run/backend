package io.openur.domain.bung.event;

import io.openur.domain.bung.model.Bung;
import io.openur.domain.challenge.repository.ChallengeRepositoryImpl;
import io.openur.domain.user.repository.UserRepositoryImpl;
import io.openur.domain.userchallenge.model.UserChallenge;
import io.openur.domain.userchallenge.repository.UserChallengeRepositoryImpl;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class BungEventListeners {

    private final UserChallengeRepositoryImpl userChallengeRepository;
    private final ChallengeRepositoryImpl challengeRepository;
    private final UserRepositoryImpl userRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional
    public void bungIsComplete(BungIsCompleteEvent event) {
        List<UserChallenge> challengesToUpdate = event.getUserIds().stream()
            .flatMap(userId -> event.getChallengeIds().stream()
                .map(challengeId -> processChallenge(event.getBung(), userId, challengeId)))
            .filter(userChallenge -> userChallenge != null)
            .toList();

        if (!challengesToUpdate.isEmpty()) {
            userChallengeRepository.saveAll(challengesToUpdate);
        }
    }

    private UserChallenge processChallenge(Bung bung, String userId, Long challengeId) {
        // Find or create UserChallenge
        UserChallenge userChallenge = userChallengeRepository
            .findOptionalByUserIdAndChallengeId(userId, challengeId)
            .orElseGet(() -> createNewUserChallenge(userId, challengeId));

        // If challenge is already completed, do nothing
        if (userChallenge.getNftCompleted()) {
            return null;
        }

        // Process challenge completion
        if (isCompletedConditionMet(bung, userChallenge)) {
            // TODO: airdrop NFT
            userChallenge.setNftCompleted(true);
        } else {
            updateCompletedCondition(userChallenge);
        }
        
        return userChallenge;
    }

    private UserChallenge createNewUserChallenge(String userId, Long challengeId) {
        return new UserChallenge(
            null,
            userRepository.findById(userId),
            challengeRepository.findById(challengeId),
            null,
            false,
            1L
        );
    }

    private boolean isCompletedConditionMet(Bung bung, UserChallenge userChallenge) {
        return switch (userChallenge.getChallenge().getCompletedType()) {
            case count -> userChallenge.getCurrentCount() >= userChallenge.getChallenge().getCompletedConditionCount();
            case date, place, wearing -> false;
            // TODO: Implement logic for date, place, and wearing conditions. `bung` will be used here.
        };
    }

    private void updateCompletedCondition(UserChallenge userChallenge) {
        switch (userChallenge.getChallenge().getCompletedType()) {
            case count -> userChallenge.setCurrentCount(userChallenge.getCurrentCount() + 1);
            case date, place, wearing -> {
                // TODO: Implement logic for date, place, and wearing conditions
            }
        }
    }
}
