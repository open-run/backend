package io.openur.domain.userbung.repository;

import io.openur.domain.userbung.entity.UserBungEntity;
import io.openur.domain.userbung.model.UserBung;
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
    public UserBung findCurrentHost(String bungId) {
        return userBungJpaRepository.findCurrentHost_Query(bungId);
    }


}
