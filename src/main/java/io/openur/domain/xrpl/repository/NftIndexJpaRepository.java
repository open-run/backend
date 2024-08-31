package io.openur.domain.xrpl.repository;

import io.openur.domain.xrpl.entity.NftIndexEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NftIndexJpaRepository extends JpaRepository<NftIndexEntity, String> {

}
