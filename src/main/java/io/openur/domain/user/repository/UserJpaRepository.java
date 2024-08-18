package io.openur.domain.user.repository;

import io.openur.domain.user.entity.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserEntity,Long> {
    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByUserId(String userId);

    List<UserEntity> findAllByUserIdIn(List<String> userIds);

    boolean existsByNickname(String nickname);
}
