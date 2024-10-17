package io.openur.domain.userbung.repository;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;
import static io.openur.domain.bung.entity.QBungEntity.bungEntity;
import static io.openur.domain.bung.model.BungStatus.PARTICIPATING;
import static io.openur.domain.user.entity.QUserEntity.userEntity;
import static io.openur.domain.userbung.entity.QUserBungEntity.userBungEntity;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.openur.domain.bung.dto.BungDetailDto;
import io.openur.domain.bung.entity.BungEntity;
import io.openur.domain.bung.model.Bung;
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
public class UserBungRepositoryImpl implements UserBungRepository, UserBungDAO {
    private final UserBungJpaRepository userBungJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public BungDetailDto findJoinedUsersByBungId(String bungId) {
        Map<BungEntity, List<UserBungEntity>> contents = queryFactory
            .select(userBungEntity, bungEntity)
            .from(userBungEntity)
            .join(userBungEntity.bungEntity, bungEntity)
            .join(userBungEntity.userEntity, userEntity)
            .where(bungEntity.bungId.eq(bungId))
            .transform(groupBy(bungEntity).as(list(userBungEntity)));

        for(Entry<BungEntity, List<UserBungEntity>> entry : contents.entrySet()) {
            return new BungDetailDto(Bung.from(entry.getKey()), entry.getValue().stream().map(UserBung::from).toList());
        }

        return null;
    }

    @Override
    public Page<Bung> findJoinedBungsByUserWithStatus(
        User user, Boolean isOwned, BungStatus status, Pageable pageable)
    {
        List<Bung> contents = queryFactory
            .selectDistinct(userBungEntity.bungEntity)
            .from(userBungEntity)
            .join(userBungEntity.bungEntity, bungEntity)
            .where(
                userBungEntity.userEntity.eq(user.toEntity()),
                ownedBungsOnly(isOwned),
                withStatus(status)
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch().stream().map(Bung::from)
            .toList();

        JPAQuery<Long> count = queryFactory
            .selectDistinct(userBungEntity.bungEntity.count())
            .from(userBungEntity)
            .join(userBungEntity.bungEntity, bungEntity)
            .where(
                userBungEntity.userEntity.eq(user.toEntity()),
                ownedBungsOnly(isOwned),
                withStatus(status)
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

    private BooleanExpression ownedBungsOnly(Boolean isOwned) {
        if(isOwned == null) return null;
        return userBungEntity.isOwner.eq(isOwned);
    }

    private BooleanExpression withStatus(BungStatus status) {
        if(status == null) return null;

        if(PARTICIPATING.equals(status)) return bungEntity.startDateTime.lt(LocalDateTime.now());
        return bungEntity.endDateTime.loe(LocalDateTime.now());
    }
}
