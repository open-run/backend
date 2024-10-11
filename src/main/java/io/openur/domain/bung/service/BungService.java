package io.openur.domain.bung.service;

import io.openur.domain.bung.dto.BungDetailDto;
import io.openur.domain.bung.dto.BungInfoDto;
import io.openur.domain.bung.dto.CreateBungDto;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.bung.model.BungStatus;
import io.openur.domain.bung.repository.BungRepositoryImpl;
import io.openur.domain.user.model.User;
import io.openur.domain.user.repository.UserRepositoryImpl;
import io.openur.domain.userbung.model.UserBung;
import io.openur.domain.userbung.repository.UserBungRepositoryImpl;
import io.openur.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BungService {
    private final BungRepositoryImpl bungRepository;
    private final UserRepositoryImpl userRepository;
    private final UserBungRepositoryImpl userBungRepository;

    @Transactional
    public BungInfoDto createBung(@AuthenticationPrincipal UserDetailsImpl userDetails,
        CreateBungDto dto) {
        User user = userRepository.findByEmail(userDetails.getUser().getEmail());

        Bung bung = new Bung(dto);
        bung = bungRepository.save(bung);

        UserBung userBung = UserBung.isOwnerBung(user, bung);
        userBungRepository.save(userBung);

        return new BungInfoDto(bung);
    }

    public BungDetailDto getBungDetail(@AuthenticationPrincipal UserDetailsImpl userDetails, String bungId) {
        return userBungRepository.findJoinedUsersByBungId(bungId);
//        return new BungInfoDto(bungRepository.findByBungId(bungId));
    }

    @PreAuthorize("@methodSecurityService.isOwnerOfBung(#userDetails, #bungId)")
    public void deleteBung(UserDetailsImpl userDetails, String bungId) {
        bungRepository.deleteByBungId(bungId);
    }

    public Page<BungInfoDto> getBungLists(UserDetailsImpl userDetails,
        BungStatus status, Pageable pageable) {
        User user = userRepository.findByEmail(userDetails.getUser().getEmail());

        if(BungStatus.hasJoined(status))
            return userBungRepository
                .findJoinedBungsByUserWithStatus(user, status, pageable)
                .map(BungInfoDto::new);

        return bungRepository
            .findBungsWithStatus(user, status, pageable)
            .map(BungInfoDto::new);
    }

    public Page<BungInfoDto> getMyBungLists(UserDetailsImpl userDetails,
        Pageable pageable) {
        User user = userRepository.findByEmail(userDetails.getUser().getEmail());

        return userBungRepository.findMyBungs(user, pageable).map(BungInfoDto::new);
    }
}
