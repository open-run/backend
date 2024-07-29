package io.openur.domain.userbung.repository;

import io.openur.domain.userbung.entity.UserBungEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBungJpaRepository extends JpaRepository<UserBungEntity, Long> {

	Optional<UserBungEntity> findByUserEntity_UserIdAndBungEntity_BungId(String userId,
		String bungId);

	List<UserBungEntity> findByBungEntity_BungId(String bungId);

    List<UserBungEntity> findByUserEntity_UserIdAndIsOwnerTrue(String userId);
}
