package io.openur.domain.xrpl.repository;

import io.openur.domain.xrpl.model.NftIndex;
import java.util.List;

public interface NftIndexRepository {

	NftIndex save(NftIndex nftIndex);

	List<String> findByUserId(String userId);
}
