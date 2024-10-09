package io.openur.domain.bung.service;

import io.openur.domain.bung.dto.BungDetailDto;
import io.openur.domain.bung.dto.PostBungEntityDto;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.bung.repository.BungRepositoryImpl;
import io.openur.domain.bunghashtag.repository.BungHashtagRepositoryImpl;
import io.openur.domain.user.model.User;
import io.openur.domain.user.repository.UserRepositoryImpl;
import io.openur.domain.userbung.model.UserBung;
import io.openur.domain.userbung.repository.UserBungRepositoryImpl;
import io.openur.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import java.util.List;
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
    private final BungHashtagRepositoryImpl bungHashtagRepository;

    private BungDetailDto buildBungDetailDto(Bung bung) {
        List<String> hashtags = bungHashtagRepository.findHashtagStrsByBungId(bung.getBungId());
        return new BungDetailDto(bung, hashtags);
    }

    @Transactional
    public BungDetailDto createBung(@AuthenticationPrincipal UserDetailsImpl userDetails,
            PostBungEntityDto dto) {
        User user = userRepository.findByEmail(userDetails.getUser().getEmail());

        Bung bung = new Bung(dto);
        bung = bungRepository.save(bung);

        UserBung userBung = UserBung.isOwnerBung(user, bung);
        userBungRepository.save(userBung);

        return this.buildBungDetailDto(bung);
    }

    public BungDetailDto getBungDetail(@AuthenticationPrincipal UserDetailsImpl userDetails,
            String bungId) {

        return this.buildBungDetailDto(bungRepository.findByBungId(bungId));
    }

    @PreAuthorize("@methodSecurityService.isOwnerOfBung(#userDetails, #bungId)")
    public void deleteBung(UserDetailsImpl userDetails, String bungId) {
        bungRepository.deleteByBungId(bungId);
    }
}
