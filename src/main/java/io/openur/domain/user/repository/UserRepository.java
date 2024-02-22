package io.openur.domain.user.repository;


import io.openur.domain.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;


public interface UserRepository {

    // ex
    public void save(UserEntity userEntity);
}
