package io.openur.domain.challenge.repository;
import io.openur.domain.challenge.entity.ChallengeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeJpaRepository extends JpaRepository<ChallengeEntity, Long> {
}
