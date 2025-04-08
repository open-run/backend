package io.openur.domain.userchallenge.repository;

import static io.openur.domain.userchallenge.entity.QUserChallengeEntity.userChallengeEntity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import io.openur.domain.user.entity.UserEntity;
import io.openur.domain.userchallenge.dto.UserChallengeInfoDto;
import io.openur.domain.userchallenge.model.UserChallenge;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class UserChallengeRepositoryImpl implements UserChallengeRepository {

    private final UserChallengeJpaRepository userChallengeJpaRepository;
    private final JPAQueryFactory queryFactory;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public UserChallenge save(UserChallenge userChallenge) {
        return UserChallenge.from(
            userChallengeJpaRepository.save(userChallenge.toEntity()));
    }


    @Override
    @Transactional
    public void bulkIncrementCount(List<Long> userChallengeIds) {
        if (userChallengeIds.isEmpty()) {
            return;
        }

        queryFactory
            .update(userChallengeEntity)
            .set(userChallengeEntity.currentCount, userChallengeEntity.currentCount.add(1))
            .where(userChallengeEntity.userChallengeId.in(userChallengeIds))
            .execute();

        entityManager.flush();
        entityManager.clear();
    }


    @Override
    @Transactional
    public void bulkUpdateCompletedChallenges(List<Long> userChallengeIds) {
        if (userChallengeIds.isEmpty()) {
            return;
        }

        queryFactory
            .update(userChallengeEntity)        
            .set(userChallengeEntity.completedDate, LocalDateTime.now())
            .set(userChallengeEntity.nftCompleted, true)
            .where(userChallengeEntity.userChallengeId.in(userChallengeIds))
            .execute();

        entityManager.flush();
        entityManager.clear();
    }

    @Override
    public List<UserChallenge> findByUserId(String userId) {
        return userChallengeJpaRepository.findAllByUserEntity_UserId(userId)
            .stream()
            .map(UserChallenge::from)
            .toList();
    }
    
    @Override
    public Page<UserChallengeInfoDto> findAllByUserEntity(UserEntity userEntity,
        Pageable pageable) {
        return userChallengeJpaRepository
            .findAllByUserEntity(userEntity, pageable)
            .map(UserChallenge::from)
            .map(UserChallengeInfoDto::new);
    }

    @Override
    public List<UserChallenge> findByUserIdsAndChallengeIds(List<String> userIds, List<Long> challengeIds) {
        return userChallengeJpaRepository
            .findAllByUserEntity_UserIdInAndChallengeIdIn(userIds, challengeIds)
            .stream()
            .map(UserChallenge::from)
            .toList();
    }

    @Override
    public Optional<UserChallenge> findOptionalByUserIdAndChallengeId(String userId, Long challengeId) {
        return userChallengeJpaRepository
            .findByUserEntity_UserIdAndChallengeId(userId,
                challengeId).map(UserChallenge::from);
    }

    @Override
    public boolean existsByUserIdAndChallengeId(String userId, Long
        challengeId) {
        return userChallengeJpaRepository.existsByUserEntity_UserIdAndChallengeId(
            userId, challengeId);
    }

    @Override
    public void delete(UserChallenge userChallenge) {
        userChallengeJpaRepository.delete(userChallenge.toEntity());
    }
}
