package io.openur.domain.userchallenge.repository;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;
import static io.openur.domain.challenge.entity.QChallengeEntity.challengeEntity;
import static io.openur.domain.challenge.entity.QChallengeStageEntity.challengeStageEntity;
import static io.openur.domain.userchallenge.entity.QUserChallengeEntity.userChallengeEntity;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.openur.domain.challenge.entity.QChallengeEntity;
import io.openur.domain.challenge.entity.QChallengeStageEntity;
import io.openur.domain.challenge.enums.CompletedType;
import io.openur.domain.userchallenge.dto.UserChallengeInfoDto;
import io.openur.domain.userchallenge.entity.QUserChallengeEntity;
import io.openur.domain.userchallenge.entity.UserChallengeEntity;
import io.openur.domain.userchallenge.model.UserChallenge;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    public Page<UserChallenge> findUncompletedChallengesByUserId(
        String userId, Pageable pageable
    ) {
        final LocalDateTime currentTime = LocalDateTime.now();

        // 1. 페이징 content 쿼리
        List<UserChallengeEntity> content = queryFactory
            .selectFrom(userChallengeEntity)
            .leftJoin(userChallengeEntity.challengeStageEntity, challengeStageEntity)
            .leftJoin(challengeStageEntity.challengeEntity, challengeEntity)
            .fetchJoin()
            .where(
                userChallengeEntity.userEntity.userId.eq(userId),
                userChallengeEntity.completedDate.isNull(),
                challengeEntity.conditionAsDate.goe(currentTime)
            )
            .orderBy(
                // 달성 퍼센테이지가 높은 것 부터
                userChallengeEntity.currentCount.desc().nullsLast()
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        if (content.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }


        // 2. Count 쿼리 최적화 (fetchJoin 불필요)
        JPAQuery<Long> countQuery = queryFactory
            .select(userChallengeEntity.count())
            .leftJoin(userChallengeEntity.challengeStageEntity, challengeStageEntity)
            .leftJoin(challengeStageEntity.challengeEntity, challengeEntity)
            .where(
                userChallengeEntity.userEntity.userId.eq(userId),
                userChallengeEntity.completedDate.isNull(),
                challengeEntity.conditionAsDate.goe(currentTime)
            );

        // 3. 엔티티 → 도메인 모델 매핑
        List<UserChallenge> result = content.stream()
            .map(UserChallenge::from)
            .toList();

        // 4. Page 반환 (countQuery는 필요 시에만 실행)
        return PageableExecutionUtils.getPage(
            result, pageable, countQuery::fetchOne
        );
    }

    @Override
    public Page<UserChallenge> findCompletedChallengesByUserId(
        String userId, Pageable pageable
    ) {
        // 1. 입력 검증
        if (!StringUtils.hasText(userId)) {
            return Page.empty(pageable);
        }

        // 2. Q클래스 재사용 (메모리 효율성)
        QUserChallengeEntity userChallenge = userChallengeEntity;

        // 4. Content 쿼리 (N+1 문제 방지)
        List<UserChallengeEntity> content = queryFactory
            .selectFrom(userChallengeEntity)
            .leftJoin(userChallengeEntity.challengeStageEntity, challengeStageEntity)
            .leftJoin(challengeStageEntity.challengeEntity, challengeEntity)
            .where(
                userChallengeEntity.userEntity.userId.eq(userId),
                userChallengeEntity.completedDate.isNotNull()
            )
            .orderBy(
                userChallengeEntity.completedDate.desc(),
                userChallengeEntity.userChallengeId.desc()
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        // 5. 빈 결과 조기 반환 (성능 최적화)
        if (content.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // 6. Count 쿼리 최적화 (fetchJoin 제거)
        JPAQuery<Long> countQuery = queryFactory
            .select(userChallenge.count())
            .from(userChallenge)
            .where(
                userChallengeEntity.userEntity.userId.eq(userId),
                userChallengeEntity.completedDate.isNotNull()
            );

        // 7. 도메인 모델 매핑 (병렬 처리 제거)
        List<UserChallenge> result = content.stream()
            .map(UserChallenge::from)
            .collect(Collectors.toList());

        return PageableExecutionUtils.getPage(result, pageable, countQuery::fetchOne);
    }



    @Override
    public Page<UserChallengeInfoDto> findAllByUserId(String string,
        Pageable pageable) {
        return userChallengeJpaRepository
            .findAllByUserEntity_UserId(string, pageable)
            .map(UserChallengeInfoDto::new);
    }

    @Override
    public List<UserChallenge> findByUserIdsAndChallengeIds(
        List<String> userIds, List<Long> challengeIds
    ) {
        return userChallengeJpaRepository
            .findAllByUserEntity_UserIdInAndChallengeEntity_ChallengeIdIn(
                userIds, challengeIds
            ).stream()
            .map(UserChallenge::from)
            .toList();
    }

    @Override
    public Map<CompletedType, List<UserChallenge>>
    findByUserIdsAndChallengeIdsGroupByCompletedType(List<String> userIds, List<Long> challengeIds)
    {
        Map<CompletedType, List<UserChallengeEntity>> entityMap = queryFactory
            .selectFrom(userChallengeEntity)
            .leftJoin(userChallengeEntity.challengeStageEntity, challengeStageEntity)
            .leftJoin(challengeStageEntity.challengeEntity, challengeEntity)
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

    private BooleanExpression withCompleteType(CompletedType type) {
        if(type != null)
            return challengeEntity.completedType.eq(type);
        return null;
    }
}
