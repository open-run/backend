package io.openur.domain.userbung.repository;

import io.openur.domain.userbung.model.UserBung;

public interface UserBungRepository {

    UserBung save(UserBung userBung);

    UserBung findByUserIdAndBungId(String userId, String bungId);

    UserBung findCurrentHost(String bungId);
}
