package io.openur.domain.userbung.repository;

import io.openur.domain.userbung.entity.UserBungEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserBungRepositoryImpl implements UserBungRepository {
    private final UserBungJpaRepository userBungJpaRepository;

    @Override
    public UserBungEntity save(UserBungEntity userBungEntity) {
        return userBungJpaRepository.save(userBungEntity);
    }
}
