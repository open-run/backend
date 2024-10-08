package io.openur.domain.bung.repository;

import static io.openur.domain.bung.entity.QBungEntity.bungEntity;
import static io.openur.domain.user.entity.QUserEntity.userEntity;
import static io.openur.domain.userbung.entity.QUserBungEntity.userBungEntity;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.openur.domain.bung.dto.BungDetailDto;
import io.openur.domain.bung.entity.BungEntity;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.bung.repository.dao.BungDAO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BungRepositoryImpl implements BungRepository, BungDAO {
    private final BungJpaRepository bungJpaRepository;


    @Override
    public Bung save(Bung bung) {
        return Bung.from(bungJpaRepository.save(bung.toEntity()));
    }

    @Override
    public Bung findByBungId(String bungId) {
        BungEntity bungEntity = bungJpaRepository.findByBungId(bungId)
            .orElseThrow(() -> new NoSuchElementException("Bung not found"));
        return Bung.from(bungEntity);
    }

    @Override
    public void deleteByBungId(String bungId) {
        bungJpaRepository.deleteByBungId(bungId);
    }

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<BungDetailDto> findBungs(Pageable pageable) {
        List<BungDetailDto> contents = queryFactory
            .selectDistinct(bungEntity)
            .from(bungEntity)
            .where(
                bungEntity.startDateTime.goe(LocalDateTime.now())
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(bungEntity.startDateTime.desc())
            .fetch().stream().map(entity -> new BungDetailDto(Bung.from(entity))).toList();

        JPAQuery<Long> count = queryFactory
            .selectDistinct(bungEntity.countDistinct())
            .from(bungEntity)
            .where(
                bungEntity.startDateTime.goe(LocalDateTime.now())
            );

        return PageableExecutionUtils.getPage(contents, pageable, count::fetchOne);
    }

    @Override
    public Page<BungDetailDto> findOwnedBungs(String userId, Pageable pageable) {
        List<BungDetailDto> contents = queryFactory
            .selectDistinct(userBungEntity.bungEntity)
            .from(userBungEntity)
            .join(userBungEntity.bungEntity, bungEntity)
            .join(userBungEntity.userEntity, userEntity)
            .where(
                userBungEntity.isOwner.isTrue(),
                userBungEntity.userEntity.userId.eq(userId)
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(bungEntity.startDateTime.desc())
            .fetch().stream().map(entity -> new BungDetailDto(Bung.from(entity))).toList();

        JPAQuery<Long> count = queryFactory
            .selectDistinct(userBungEntity.bungEntity.countDistinct())
            .from(userBungEntity)
            .join(userBungEntity.bungEntity, bungEntity)
            .join(userBungEntity.userEntity, userEntity)
            .where(
                userBungEntity.isOwner.isTrue(),
                userBungEntity.userEntity.userId.eq(userId)
            );

        return PageableExecutionUtils.getPage(contents, pageable, count::fetchOne);
    }
}
