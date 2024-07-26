package io.openur.domain.bung.repository;

import io.openur.domain.bung.entity.BungEntity;
import io.openur.domain.bung.model.Bung;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BungRepositoryImpl implements BungRepository {
    private final BungJpaRepository bungJpaRepository;

    @Override
    public Bung save(Bung bung) {
        return Bung.from(bungJpaRepository.save(bung.toEntity()));
    }

    @Override
    public Bung findByBungId(String bungId) {
        BungEntity bungEntity = bungJpaRepository.findByBungId(bungId)
            .orElseThrow(() -> new NoSuchElementException("Bung not found"));
        return Bung.from(bungEntity);
    }

    @Override
    public void deleteByBungId(String bungId) {
        bungJpaRepository.deleteByBungId(bungId);
    }

}
