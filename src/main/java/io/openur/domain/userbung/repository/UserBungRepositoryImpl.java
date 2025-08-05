package io.openur.domain.userbung.repository;


import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;
import static io.openur.domain.bung.entity.QBungEntity.bungEntity;
import static io.openur.domain.bung.enums.BungStatus.ACCOMPLISHED;
import static io.openur.domain.bunghashtag.entity.QBungHashtagEntity.bungHashtagEntity;
import static io.openur.domain.hashtag.entity.QHashtagEntity.hashtagEntity;
import static io.openur.domain.user.entity.QUserEntity.userEntity;
import static io.openur.domain.userbung.entity.QUserBungEntity.userBungEntity;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.openur.domain.bung.dto.BungInfoWithMemberListDto;
import io.openur.domain.bung.entity.BungEntity;
import io.openur.domain.bung.enums.BungStatus;
import io.openur.domain.user.model.User;
import io.openur.domain.userbung.entity.UserBungEntity;
import io.openur.domain.userbung.model.UserBung;
import java.awt.HeadlessException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
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
public class UserBungRepositoryImpl implements UserBungRepository {

    private final UserBungJpaRepository userBungJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public int countParticipantsByBungId(String bungId) {
        return queryFactory
            .select(userBungEntity.count())
            .from(userBungEntity)
            .where(userBungEntity.bungEntity.bungId.eq(bungId))
            .fetchOne()
            .intValue();
    }

    @Override
    public Optional<BungInfoWithMemberListDto> findBungWithUsersById(String bungId) {
        List<UserBungEntity> members = queryFactory
            .selectFrom(userBungEntity)
            .join(userBungEntity.userEntity).fetchJoin()
            .join(userBungEntity.bungEntity, bungEntity).fetchJoin()
            .leftJoin(bungEntity.bungHashtags, bungHashtagEntity).fetchJoin()
            .where(userBungEntity.bungEntity.bungId.eq(bungId))
            .fetch();

        if (members.isEmpty()) {
            return Optional.empty();
        }

        // 첫 번째 멤버에서 BungEntity 추출 (모든 멤버는 동일한 Bung을 참조)
        BungEntity bung = members.get(0).getBungEntity();

        return Optional.of(new BungInfoWithMemberListDto(bung, members));
    }

    @Override
    public Page<BungInfoWithMemberListDto> findBungsWithUserName(String nickname, Pageable pageable) {
        if (!StringUtils.hasText(nickname)) {
            return Page.empty(pageable);
        }

        // 조건을 명확히 분리
        BooleanExpression nicknameCondition = userBungEntity.userEntity.nickname.containsIgnoreCase(nickname);
        BooleanExpression dateCondition = bungEntity.startDateTime.gt(LocalDateTime.now());

        // 1. 서브쿼리로 페이징 및 정렬된 BungEntity ID 조회
        List<String> bungIds = queryFactory
            .selectDistinct(userBungEntity.bungEntity)
            .from(userBungEntity)
            .join(userBungEntity.bungEntity, bungEntity)
            .where(nicknameCondition, dateCondition)
            .orderBy(bungEntity.startDateTime.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch()
            .stream().map(BungEntity::getBungId).toList();

        if (bungIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // 2. 상세 데이터 조회 및 fetchJoin으로 N+1 문제 방지
        Map<String, List<UserBungEntity>> bungMap = queryFactory
            .selectFrom(userBungEntity)
            .join(userBungEntity.bungEntity, bungEntity).fetchJoin()
            .join(userBungEntity.userEntity, userEntity).fetchJoin()
            .where(bungEntity.bungId.in(bungIds))
            .transform(groupBy(bungEntity.bungId).as(list(userBungEntity)));

        // 3. 정렬 순서 유지하며 DTO 변환
        List<BungInfoWithMemberListDto> contents = bungIds.stream()
            .map(bungMap::get)
            .filter(Objects::nonNull)
            .map(members -> new BungInfoWithMemberListDto(members.get(0).getBungEntity(), members))
            .toList();

        // 4. 정확한 카운트 쿼리 (DISTINCT 사용)
        long total = queryFactory
            .select(userBungEntity.bungEntity.bungId.countDistinct())
            .from(userBungEntity)
            .join(userBungEntity.bungEntity, bungEntity)
            .where(nicknameCondition, dateCondition)
            .fetchOne();

        return new PageImpl<>(contents, pageable, total);
    }

    @Override
    public Page<UserBung> findJoinedBungsByUserWithStatus(
        User user, Boolean isOwned, BungStatus status, Pageable pageable) {
        List<UserBung> contents = queryFactory
            .selectDistinct(userBungEntity)
            .from(userBungEntity)
            .join(userBungEntity.bungEntity, bungEntity).fetchJoin()
            .leftJoin(bungEntity.bungHashtags, bungHashtagEntity).fetchJoin()
            .leftJoin(bungHashtagEntity.hashtagEntity, hashtagEntity).fetchJoin()
            .where(
                userBungEntity.userEntity.eq(user.toEntity()),
                ownedBungsOnly(isOwned),
                withStatusCondition(status)
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(withStatusOrdering(status))
            .fetch().stream().map(UserBung::from)
            .toList();

        JPAQuery<Long> count = queryFactory
            .selectDistinct(userBungEntity.countDistinct())
            .from(userBungEntity)
            .join(userBungEntity.bungEntity, bungEntity)
            .where(
                userBungEntity.userEntity.eq(user.toEntity()),
                ownedBungsOnly(isOwned),
                withStatusCondition(status)
            );

        return PageableExecutionUtils.getPage(contents, pageable, count::fetchOne);
    }

    @Override
    public List<String> findJoinedBungsId(User user) {
        return queryFactory
            .selectDistinct(userBungEntity.bungEntity.bungId)
            .from(userBungEntity)
            .join(userBungEntity.bungEntity, bungEntity)
            .where(userBungEntity.userEntity.eq(user.toEntity()))
            .fetch();
    }

    @Override
    public Page<Tuple> findAllFrequentUsers(List<String> bungIds, User user,
        Pageable pageable) {
        List<Tuple> contents = queryFactory
            .select(userBungEntity.userEntity, userBungEntity.userEntity.count())
            .from(userBungEntity)
            .join(userBungEntity.userEntity, userEntity)
            .join(userBungEntity.bungEntity, bungEntity)
            .where(
                userBungEntity.bungEntity.bungId.in(bungIds),
                userBungEntity.userEntity.ne(user.toEntity())
            )
            .groupBy(userBungEntity.userEntity)
            .orderBy(userBungEntity.userEntity.count().desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> count = queryFactory
            .select(userBungEntity.userEntity.countDistinct())
            .from(userBungEntity)
            .join(userBungEntity.userEntity, userEntity)
            .join(userBungEntity.bungEntity, bungEntity)
            .where(
                userBungEntity.bungEntity.bungId.in(bungIds),
                userBungEntity.userEntity.ne(user.toEntity())
            );

        return PageableExecutionUtils.getPage(contents, pageable, count::fetchOne);
    }

    @Override
    public UserBung findByUserIdAndBungId(String userId, String bungId) {
        UserBungEntity userBungEntity = userBungJpaRepository
            .findByUserEntity_UserIdAndBungEntity_BungId(userId, bungId)
            .orElseThrow(() ->
                new NoSuchElementException(
                    String.format(
                        "UserBung not found by given userId(%s) and bungId(%s)",
                        userId, bungId
                    )
                )
            );
        return UserBung.from(userBungEntity);
    }

    @Override
    public Boolean existsByUserIdAndBungId(String userId, String bungId) {
        return userBungJpaRepository.existsByUserEntity_UserIdAndBungEntity_BungId(userId, bungId);
    }

    @Override
    public UserBung findCurrentOwner(String bungId) {
        List<UserBungEntity> userBungEntities = userBungJpaRepository.findByBungEntity_BungId(
            bungId);
        if (userBungEntities.isEmpty()) {
            throw new NoSuchElementException(
                String.format("UserBung not found by given bungId(%s)", bungId)
            );
        }

        return UserBung.from(userBungEntities.stream()
            .filter(UserBungEntity::isOwner)
            .findFirst()
            .orElseThrow(() ->
                new HeadlessException(
                    String.format("Owner not found by given bungId(%s)", bungId)
                )
            )
        );
    }

    @Override
    public UserBung save(UserBung userBung) {
        return UserBung.from(userBungJpaRepository.save(userBung.toEntity()));
    }

    @Override
    public void removeUserFromBung(UserBung userBung) {
        userBungJpaRepository.delete(userBung.toEntity());
    }

    @Override
    public void deleteByBungId(String bungId) {
        userBungJpaRepository.deleteByBungEntity_BungId(bungId);
    }

    private BooleanExpression ownedBungsOnly(Boolean isOwned) {
        if (isOwned == null) {
            return null;
        }
        return userBungEntity.isOwner.eq(isOwned);
    }

    private BooleanExpression withStatusCondition(BungStatus status) {
        LocalDateTime now = LocalDateTime.now();
        // 끝난거
        return switch (status) {
            // 시작하기 전인 것만 보이는 경우
            case PENDING -> bungEntity.startDateTime.gt(now);
            // 참여중인, 즉 행사 시간 내 인 조건.
            case PARTICIPATING ->
                bungEntity.startDateTime.loe(now)
                .and(bungEntity.endDateTime.gt(now));
            // 시작은 했는데, 완료처리가 되지 않을 경우가 있을 수 있겠다고 생각
            case ONGOING -> bungEntity.completed.isFalse();
            // 이미 완료처리가 된 것들
            case ACCOMPLISHED -> bungEntity.completed.isTrue();

            default -> null;
        };
    }

    private OrderSpecifier<?> withStatusOrdering(BungStatus status) {
        // 완료된 벙을 볼 경우는 최근 끝난것부터 내림차순
        if (ACCOMPLISHED.equals(status)) {
            return bungEntity.endDateTime.desc();
        }

        // 진행중 및 예정인 경우는 시작 시간 오름차순 ( 임박순 )
        return bungEntity.startDateTime.asc();
    }
}
