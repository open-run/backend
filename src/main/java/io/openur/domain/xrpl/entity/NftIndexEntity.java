package io.openur.domain.xrpl.entity;

import io.openur.domain.user.entity.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "tb_nft_index")
@NoArgsConstructor
@AllArgsConstructor
public class NftIndexEntity {

	@Id
	private String nftIndex;

	@ManyToOne(targetEntity = UserEntity.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private UserEntity userEntity;

	@Column(name = "is_new")
	private boolean isNew;

	@Column(name = "is_active")
	private boolean isActive;
}
