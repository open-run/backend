package io.openur.domain.bung.service;

import io.openur.domain.bung.dto.BungDetailDto;
import io.openur.domain.bung.dto.PostBungEntityDto;
import io.openur.domain.bung.dto.Req.InviteMembersRequestDto;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.bung.repository.BungRepositoryImpl;
import io.openur.domain.user.model.User;
import io.openur.domain.user.repository.UserRepositoryImpl;
import io.openur.domain.userbung.model.UserBung;
import io.openur.domain.userbung.repository.UserBungRepositoryImpl;
import io.openur.global.enums.BungStatus;
import io.openur.global.security.UserDetailsImpl;
import java.util.List;
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
    public BungDetailDto createBung(@AuthenticationPrincipal UserDetailsImpl userDetails,
        PostBungEntityDto dto) {
        User user = userRepository.findByEmail(userDetails.getUser().getEmail());

        Bung bung = new Bung(dto);
        bung = bungRepository.save(bung);

        UserBung userBung = UserBung.isOwnerBung(user, bung);
        userBungRepository.save(userBung);

        return new BungDetailDto(bung);
    }

    public Page<BungDetailDto> getBungLists(@AuthenticationPrincipal UserDetailsImpl userDetails, BungStatus status, Pageable pageable) {
        User user = userRepository.findByEmail(userDetails.getUser().getEmail());

        if(BungStatus.notUserFiltered(status))
            return bungRepository.findBungs(status, pageable);
        else
            return userBungRepository.findBungs(user.getUserId(), status, pageable);
    }

    public Page<BungDetailDto> getOwnedBungLists(@AuthenticationPrincipal UserDetailsImpl userDetails,
        Pageable pageable) {
        User user = userRepository.findByEmail(userDetails.getUser().getEmail());

        return bungRepository.findOwnedBungs(user.getUserId(), pageable);
    }

    public BungDetailDto getBungDetail(@AuthenticationPrincipal UserDetailsImpl userDetails,
        String bungId) {

        return new BungDetailDto(bungRepository.findByBungId(bungId));
    }

    @Transactional
    @PreAuthorize("@methodSecurityService.isOwnerOfBung(#userDetails, #bungId)")
    public void inviteUsers(@AuthenticationPrincipal UserDetailsImpl userDetails, String bungId,
                            InviteMembersRequestDto req) {
        Bung bung = bungRepository.findByBungId(bungId);

        List<User> users = userRepository.findAllByIdIn(req.getUserIds());

        //TODO : users 와 bung 으로 invitation 개념의 임시 초대 이력 bulk 생성 하고 알림을 전송
    }

    @Transactional
    @PreAuthorize("@methodSecurityService.isOwnerOfBung(#userDetails, #bungId)")
    public void deleteBung(UserDetailsImpl userDetails, String bungId) {
        bungRepository.deleteByBungId(bungId);
    }
}
