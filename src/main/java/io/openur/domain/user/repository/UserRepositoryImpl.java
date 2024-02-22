package io.openur.domain.user.repository;

import io.openur.domain.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository{
    private final UserJpaRepository userJpaRepository;

    // ex
    @Override
    public void save(UserEntity userEntity) {
        userJpaRepository.save(userEntity);
    }
}
