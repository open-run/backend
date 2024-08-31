package io.openur.domain.xrpl.repository;

import io.openur.domain.xrpl.model.NftIndex;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NftIndexRepositoryImpl implements NftIndexRepository {

	private final NftIndexJpaRepository nftIndexJpaRepository;

	@Override
	public NftIndex save(NftIndex nftIndex) {
		return NftIndex.from(nftIndexJpaRepository.save(nftIndex.toEntity()));
	}

	@Override
	public List<String> findByUserId(String userId) {
		return nftIndexJpaRepository.findByUserEntity_UserId(userId).stream()
			.map(v -> NftIndex.from(v).getNftIndex())
			.toList();
	}
}
