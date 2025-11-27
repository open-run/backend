package io.openur.domain.userchallenge.repository;

import io.openur.domain.userchallenge.entity.UserChallengeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserChallengeJpaRepository extends JpaRepository<UserChallengeEntity, Long> {

}
