package io.openur.domain.userbung.repository;

import io.openur.domain.userbung.entity.UserBungEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserBungJpaRepository extends JpaRepository<UserBungEntity, Long> {

	Optional<UserBungEntity> findByUserEntity_UserIdAndBungEntity_BungId(String userId,
		String bungId);

    @Query("SELECT ub FROM UserBungEntity ub WHERE ub.bungEntity.id = :bungId AND ub.isHost = true")
    Optional<UserBungEntity> findCurrentHost_Query(@Param("bungId") String bungId);

}
