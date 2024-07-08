package io.openur.domain.userbung.service;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.bung.repository.BungRepositoryImpl;
import io.openur.domain.user.model.User;
import io.openur.domain.user.repository.UserRepositoryImpl;
import io.openur.domain.userbung.model.UserBung;
import io.openur.domain.userbung.repository.UserBungRepositoryImpl;
import io.openur.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserBungService {
    private final BungRepositoryImpl bungRepository;
    private final UserRepositoryImpl userRepository;
    private final UserBungRepositoryImpl userBungRepository;

    @Transactional
    public void removeUserFromBung(String bungId,String userIdToRemove) {
        UserBung userBung = userBungRepository.findByUserIdAndBungId(userIdToRemove, bungId);



        userBungRepository.deleteByUserEntity_UserIdAndBungEntity_BungId(userIdToRemove, bungId);
        userBungRepository.save(userBung);
    }
}
