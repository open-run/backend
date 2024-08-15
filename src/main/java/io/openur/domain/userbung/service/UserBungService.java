package io.openur.domain.userbung.service;

import io.openur.domain.userbung.model.UserBung;
import io.openur.domain.userbung.repository.UserBungRepositoryImpl;
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

	@Transactional
	@PreAuthorize("@methodSecurityService.isOwnerOfBung(#userDetails, #bungId)")
	public void changeOwner(UserDetailsImpl userDetails, String bungId, String newOwnerUserId) {
		UserBung currentOwner = userBungRepository.findCurrentOwner(bungId);

		currentOwner.disableOwnerBung();
		userBungRepository.save(currentOwner);

		UserBung newOwner = userBungRepository.findByUserIdAndBungId(newOwnerUserId, bungId);
		newOwner.enableOwnerBung();
		userBungRepository.save(newOwner);
	}

	@Transactional
	@PreAuthorize("@methodSecurityService.isOwnerOfBung(#userDetails, #bungId)")
	public void removeUserFromBung(UserDetailsImpl userDetails, String bungId,
		String userIdToRemove) {
		UserBung userBung = userBungRepository.findByUserIdAndBungId(userIdToRemove, bungId);
		userBungRepository.removeUserFromBung(userBung);
	}
}
