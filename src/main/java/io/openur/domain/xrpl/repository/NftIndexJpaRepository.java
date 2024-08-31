package io.openur.domain.xrpl.repository;

import io.openur.domain.xrpl.entity.NftIndexEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NftIndexJpaRepository extends JpaRepository<NftIndexEntity, String> {

	List<NftIndexEntity> findByUserEntity_UserId(String userId);
}
