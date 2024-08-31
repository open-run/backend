package io.openur.domain.xrpl.model;

import io.openur.domain.user.model.User;
import io.openur.domain.xrpl.entity.NftIndexEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NftIndex {

	private String nftIndex;
	private User user;
	private boolean isNew;
	private boolean isActive;

	public NftIndex(String nftIndex, User user) {
		this.nftIndex = nftIndex;
		this.user = user;
		this.isNew = true;
		this.isActive = true;
	}

	public static NftIndex from(NftIndexEntity nftIndexEntity) {
		return new NftIndex(
			nftIndexEntity.getNftIndex(),
			User.from(nftIndexEntity.getUserEntity()),
			nftIndexEntity.isNew(),
			nftIndexEntity.isActive()
		);
	}

	public NftIndexEntity toEntity() {
		return new NftIndexEntity(
			this.nftIndex,
			this.user.toEntity(),
			this.isNew,
			this.isActive
		);
	}
}
