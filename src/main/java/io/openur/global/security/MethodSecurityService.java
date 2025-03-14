package io.openur.global.security;

import io.openur.domain.userbung.model.UserBung;
import io.openur.domain.userbung.repository.UserBungRepositoryImpl;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MethodSecurityService {

    private final UserBungRepositoryImpl userBungRepository;

    public boolean isOwnerOfBung(@AuthenticationPrincipal UserDetailsImpl userDetails,
        String bungId) {
        try {
            UserBung userBung = userBungRepository.findByUserIdAndBungId(
                userDetails.getUser().getUserId(), bungId);
            return userBung.isOwner();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public boolean isSelf(@AuthenticationPrincipal UserDetailsImpl userDetails,
        String userId) {
        return userDetails.getUser().getUserId().equals(userId);
    }

    public boolean isBungParticipant(@AuthenticationPrincipal UserDetailsImpl userDetails,
        String bungId) {
        return userBungRepository.existsByUserIdAndBungId(userDetails.getUser().getUserId(), bungId);
    }
}

