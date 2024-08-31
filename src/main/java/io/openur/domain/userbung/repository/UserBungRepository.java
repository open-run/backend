package io.openur.domain.userbung.repository;

import io.openur.domain.bung.model.Bung;
import io.openur.domain.userbung.model.UserBung;

import java.util.List;

public interface UserBungRepository {

    UserBung save(UserBung userBung);

    UserBung findByUserIdAndBungId(String userId, String bungId);

    UserBung findCurrentOwner(String bungId);

    void removeUserFromBung(UserBung userBung);
}
