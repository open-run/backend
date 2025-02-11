package io.openur.domain.userbung.service;

import io.openur.domain.userbung.exception.RemoveUserFromBungException;
import io.openur.domain.userbung.model.UserBung;
import io.openur.domain.userbung.repository.UserBungRepositoryImpl;
import io.openur.global.security.MethodSecurityService;
import io.openur.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserBungService {

    private final UserBungRepositoryImpl userBungRepository;
    private final MethodSecurityService methodSecurityService;

    @Transactional
    @PreAuthorize("@methodSecurityService.isOwnerOfBung(#userDetails, #bungId)")
    public void changeOwner(UserDetailsImpl userDetails, String bungId, String newOwnerUserId) {
        UserBung currentOwner = userBungRepository.findCurrentOwner(bungId);

        // TODO: 벙주 넘겨받을 유저에게 알람, 수락 시 변경
        currentOwner.disableOwnerBung();
        userBungRepository.save(currentOwner);

        UserBung newOwner = userBungRepository.findByUserIdAndBungId(newOwnerUserId, bungId);
        newOwner.enableOwnerBung();
        userBungRepository.save(newOwner);
    }

    @Transactional
    public void removeUserFromBung(UserDetailsImpl userDetails, String bungId,
        String userIdToRemove) throws RemoveUserFromBungException {
        Boolean isOwner = methodSecurityService.isOwnerOfBung(userDetails, bungId);
        Boolean isSelf = methodSecurityService.isSelf(userDetails, userIdToRemove);
        if (isOwner && isSelf) {
            throw new RemoveUserFromBungException("Owners cannot remove themselves from bung.");
        } else if (!isOwner && !isSelf) {
            throw new RemoveUserFromBungException(
                "Must be the owner of bung or self to remove user from bung.");
        }

        UserBung userBung = userBungRepository.findByUserIdAndBungId(userIdToRemove, bungId);
        userBungRepository.removeUserFromBung(userBung);
    }

    @Transactional
    @PreAuthorize("@methodSecurityService.isBungParticipant(#userDetails, #bungId)")
    public void confirmBungParticipation(UserDetailsImpl userDetails, String bungId) {
        UserBung userBung = userBungRepository.findByUserIdAndBungId(userDetails.getUser().getUserId(), bungId);
        userBung.setParticipationStatus(true);
        userBungRepository.save(userBung);
    }
}
