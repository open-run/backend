package io.openur.domain.userbung.repository;

import io.openur.domain.user.entity.UserEntity;
import io.openur.domain.userbung.entity.UserBungEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBungJpaRepository extends JpaRepository<UserBungEntity, Long> {

    Optional<UserBungEntity> findByUserEntity_UserIdAndBungEntity_BungId(String userId,
        String bungId);

    Page<UserBungEntity> findAllByUserEntityAndOwnerIsTrueOrderByUserBungIdDesc(UserEntity userEntity,
        Pageable pageable);

    List<UserBungEntity> findByBungEntity_BungId(String bungId);

    void deleteByBungEntity_BungId(String bungId);

}
