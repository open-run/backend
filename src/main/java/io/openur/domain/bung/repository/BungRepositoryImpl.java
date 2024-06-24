package io.openur.domain.bung.repository;

import io.openur.domain.bung.entity.BungEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BungRepositoryImpl implements BungRepository {
    private final BungJpaRepository bungJpaRepository;

    @Override
    public BungEntity save(BungEntity bungEntity) {
        return bungJpaRepository.save(bungEntity);
    }

    @Override
    public BungEntity findByBungId(String bungId) {
        return bungJpaRepository.findByBungId(bungId).orElse(null);
    }
}
