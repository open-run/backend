package io.openur.domain.bung.repository;

import static io.openur.domain.bung.entity.QBungEntity.bungEntity;
import static io.openur.domain.userbung.entity.QUserBungEntity.userBungEntity;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.openur.domain.bung.dto.BungDetailDto;
import io.openur.domain.bung.entity.BungEntity;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.bung.repository.dao.BungDAO;
import io.openur.domain.user.model.User;
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
    public Page<BungDetailDto> findBungs(User user, boolean isParticipating,
        Pageable pageable) {

        List<BungDetailDto> contents = queryFactory
            .selectDistinct(userBungEntity.bungEntity)
            .from(userBungEntity)
            .join(userBungEntity.bungEntity, bungEntity)
            .where(
                attendFilter(user, isParticipating)
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch().stream().map(entity -> new BungDetailDto(Bung.from(entity))).toList();

        JPAQuery<Long> count = queryFactory
            .selectDistinct(userBungEntity.bungEntity.count())
            .from(userBungEntity)
            .join(userBungEntity.bungEntity, bungEntity)
            .where(
                attendFilter(user, isParticipating)
            );

        return PageableExecutionUtils.getPage(contents, pageable, count::fetchOne);
    }

    private BooleanExpression attendFilter(User user, boolean isParticipating) {
        if(isParticipating) {
            return userBungEntity.userEntity.eq(user.toEntity())
                .and(userBungEntity.isOwner.eq(true));
        }
        return null;
    }
}
