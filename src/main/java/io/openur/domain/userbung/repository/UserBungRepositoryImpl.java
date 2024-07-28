package io.openur.domain.userbung.repository;

import io.openur.domain.userbung.entity.UserBungEntity;
import io.openur.domain.userbung.model.UserBung;
import java.awt.HeadlessException;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserBungRepositoryImpl implements UserBungRepository {
    private final UserBungJpaRepository userBungJpaRepository;

    @Override
    public UserBung save(UserBung userBung) {
        return UserBung.from(userBungJpaRepository.save(userBung.toEntity()));
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
    public void removeUserFromBung(UserBung userBung) {
        userBungJpaRepository.delete(userBung.toEntity());
    }

}
