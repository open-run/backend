package io.openur.domain.userbung.repository;

import io.openur.domain.bung.model.Bung;
import io.openur.domain.user.model.User;
import io.openur.domain.userbung.model.UserBung;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserBungRepository {

    UserBung save(UserBung userBung);

    Page<Bung> findMyBungs(User user, Pageable pageable);

    UserBung findByUserIdAndBungId(String userId, String bungId);

    UserBung findCurrentOwner(String bungId);

    void removeUserFromBung(UserBung userBung);
}
