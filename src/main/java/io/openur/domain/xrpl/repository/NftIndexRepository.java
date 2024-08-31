package io.openur.domain.xrpl.repository;

import io.openur.domain.xrpl.model.NftIndex;

public interface NftIndexRepository {

	NftIndex save(NftIndex nftIndex);
}
