package io.openur.domain.userchallenge.repository;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;
import static io.openur.domain.challenge.entity.QChallengeEntity.challengeEntity;
import static io.openur.domain.userchallenge.entity.QUserChallengeEntity.userChallengeEntity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import io.openur.domain.challenge.model.CompletedType;
import io.openur.domain.userchallenge.dto.UserChallengeInfoDto;
import io.openur.domain.userchallenge.entity.UserChallengeEntity;
import io.openur.domain.userchallenge.model.UserChallenge;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
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
    public Page<UserChallengeInfoDto> findAllByUserId(String string,
        Pageable pageable) {
        return userChallengeJpaRepository
            .findAllByUserEntity_UserId(string, pageable)
            .map(UserChallenge::from)
            .map(UserChallengeInfoDto::new);
    }

    @Override
    public List<UserChallenge> findByUserIdsAndChallengeIds(List<String> userIds, List<Long> challengeIds) {
        return userChallengeJpaRepository
            .findAllByUserEntity_UserIdInAndChallengeEntity_ChallengeIdIn(userIds, challengeIds)
            .stream()
            .map(UserChallenge::from)
            .toList();
    }

    @Override
    public Map<CompletedType, List<UserChallenge>>
    findByUserIdsAndChallengeIdsGroupByCompletedType(List<String> userIds, List<Long> challengeIds)
    {
        Map<CompletedType, List<UserChallengeEntity>> entityMap = queryFactory
            .selectFrom(userChallengeEntity)
            .join(userChallengeEntity.challengeEntity, challengeEntity).fetchJoin()
            .where(
                userChallengeEntity.userEntity.userId.in(userIds),
                userChallengeEntity.nftCompleted.isFalse(),
                challengeEntity.challengeId.in(challengeIds)
            )
            .transform(
                groupBy(challengeEntity.completedType)
                    .as(list(userChallengeEntity))
            );

        // 2. DTO 변환
        return entityMap.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream()
                    .map(UserChallenge::from).toList()
            ));
    }

    @Override
    public Optional<UserChallenge> findOptionalByUserIdAndChallengeId(String userId, Long challengeId) {
        return userChallengeJpaRepository
            .findByUserEntity_UserIdAndChallengeEntity_ChallengeId(
                userId, challengeId)
            .map(UserChallenge::from);
    }

    @Override
    public boolean existsByUserIdAndChallengeId(String userId, Long
        challengeId) {
        return userChallengeJpaRepository
            .existsByUserEntity_UserIdAndChallengeEntity_ChallengeId(
                userId, challengeId);
    }

    @Override
    public void delete(UserChallenge userChallenge) {
        userChallengeJpaRepository.delete(userChallenge.toEntity());
    }
}
