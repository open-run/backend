package io.openur.domain.userchallenge.repository;

import static io.openur.domain.userchallenge.entity.QUserChallengeEntity.userChallengeEntity;

import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.openur.domain.challenge.model.CompletedType;
import io.openur.domain.userchallenge.model.UserChallenge;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class UserChallengeRepositoryImpl implements UserChallengeRepository {

    private final UserChallengeJpaRepository userChallengeJpaRepository;
    private final JPAQueryFactory queryFactory;

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public UserChallenge save(UserChallenge userChallenge) {
        return UserChallenge.from(
            userChallengeJpaRepository.save(userChallenge.toEntity()));
    }

    @Override
    public List<UserChallenge> findByUserId(String userId) {
        return userChallengeJpaRepository.findByUserEntity_UserId(userId)
            .stream()
            .map(UserChallenge::from)
            .toList();
    }

    @Override
    @Transactional
    public void bulkUpdateChallengeProgress(List<String> userIds) {
        // 서브쿼리를 사용하여 증가된 currentCount 값을 기준으로 완료 여부 판단
        queryFactory
            .update(userChallengeEntity)
            .set(userChallengeEntity.currentCount, userChallengeEntity.currentCount.add(1))
            .set(userChallengeEntity.completedDate,
                new CaseBuilder()
                    .when(userChallengeEntity.currentCount.add(1)
                        .goe(userChallengeEntity.challengeEntity.completedConditionCount))
                    .then(LocalDateTime.now())
                    .otherwise((LocalDateTime) null))
            .where(
                userChallengeEntity.userEntity.userId.in(userIds),
                userChallengeEntity.completedDate.isNull(),
                userChallengeEntity.challengeEntity.completedType.eq(CompletedType.count)
            )
            .execute();

        entityManager.flush();
        entityManager.clear();
    }

    @Override
    public Optional<UserChallenge> findOptionalByUserIdAndChallengeId(String userId, Long challengeId){
            return userChallengeJpaRepository
                .findByUserEntity_UserIdAndChallengeEntity_ChallengeId(userId,
                    challengeId).map(UserChallenge::from);
        }

    @Override
    public boolean existsByUserIdAndChallengeId (String userId, Long
    challengeId){
        return userChallengeJpaRepository.existsByUserEntity_UserIdAndChallengeEntity_ChallengeId(
            userId, challengeId);
    }

    @Override
    public void delete (UserChallenge userChallenge){
        userChallengeJpaRepository.delete(userChallenge.toEntity());
    }
}
