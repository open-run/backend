package io.openur.domain.userbung.repository;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;
import static io.openur.domain.bung.entity.QBungEntity.bungEntity;
import static io.openur.domain.bung.model.BungStatus.ACCOMPLISHED;
import static io.openur.domain.hashtag.entity.QHashtagEntity.hashtagEntity;
import static io.openur.domain.user.entity.QUserEntity.userEntity;
import static io.openur.domain.userbung.entity.QUserBungEntity.userBungEntity;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.openur.domain.bung.dto.BungInfoWithMemberListDto;
import io.openur.domain.bung.entity.BungEntity;
import io.openur.domain.bung.model.BungStatus;
import io.openur.domain.user.model.User;
import io.openur.domain.userbung.entity.UserBungEntity;
import io.openur.domain.userbung.model.UserBung;
import java.awt.HeadlessException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class UserBungRepositoryImpl implements UserBungRepository {

    private final UserBungJpaRepository userBungJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public BungInfoWithMemberListDto findBungWithUsersById(String bungId) {
        Map<BungEntity, List<UserBungEntity>> contents = queryFactory
            .select(userBungEntity, bungEntity)
            .from(userBungEntity)
            .join(userBungEntity.bungEntity, bungEntity)
            .join(userBungEntity.userEntity, userEntity)
            .where(bungEntity.bungId.eq(bungId))
            .transform(groupBy(bungEntity).as(list(userBungEntity)));

        for (Entry<BungEntity, List<UserBungEntity>> entry : contents.entrySet()) {
            return new BungInfoWithMemberListDto(entry);
        }

        return null;
    }

    @Override
    public Page<UserBung> findJoinedBungsByUserWithStatus(
        User user, Boolean isOwned, BungStatus status, Pageable pageable) {
        List<UserBung> contents = queryFactory
            .selectDistinct(userBungEntity)
            .from(userBungEntity)
            .join(userBungEntity.bungEntity, bungEntity).fetchJoin()
            .leftJoin(bungEntity.hashtags, hashtagEntity).fetchJoin()
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
