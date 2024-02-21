package io.openur.domain.user.repository;


import io.openur.domain.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepository {
    private final UserJpaRepository userJpaRepository;

    // ex
    public void save(UserEntity userEntity) {
        userJpaRepository.save(userEntity);
    }
}
