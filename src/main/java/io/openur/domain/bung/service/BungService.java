package io.openur.domain.bung.service;

import io.openur.domain.bung.dto.BungInfoDto;
import io.openur.domain.bung.dto.BungInfoWithHashtagsDto;
import io.openur.domain.bung.dto.BungInfoWithMemberListDto;
import io.openur.domain.bung.dto.CreateBungDto;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.bung.model.BungStatus;
import io.openur.domain.bung.repository.BungRepositoryImpl;
import io.openur.domain.bunghashtag.repository.BungHashtagRepositoryImpl;
import io.openur.domain.hashtag.model.Hashtag;
import io.openur.domain.hashtag.repository.HashtagRepositoryImpl;
import io.openur.domain.user.model.User;
import io.openur.domain.user.repository.UserRepositoryImpl;
import io.openur.domain.userbung.model.UserBung;
import io.openur.domain.userbung.repository.UserBungRepositoryImpl;
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
    private final HashtagRepositoryImpl hashtagRepository;
    private final BungHashtagRepositoryImpl bungHashtagRepository;

    private Bung saveNewBung(UserDetailsImpl userDetails, CreateBungDto dto) {
        User user = userRepository.findByEmail(userDetails.getUser().getEmail());
        Bung bung = bungRepository.save(new Bung(dto));
        userBungRepository.save(UserBung.isOwnerBung(user, bung));
        return bung;
    }

    private void saveHashtags(Bung bung, List<String> hashtagStrList) {
        List<Hashtag> savedHashtags = hashtagRepository.saveAll(hashtagStrList);
        bungHashtagRepository.bulkInsertHashtags(bung, savedHashtags);
    }

    @Transactional
    public BungInfoDto createBung(@AuthenticationPrincipal UserDetailsImpl userDetails,
        CreateBungDto dto) {
        Bung bung = this.saveNewBung(userDetails, dto);
        this.saveHashtags(bung, dto.getHashtags());
        return new BungInfoWithHashtagsDto(bung, dto.getHashtags());
    }

    public BungInfoWithMemberListDto getBungDetail(@AuthenticationPrincipal UserDetailsImpl userDetails, String bungId) {
        return userBungRepository.findBungWithUsersById(bungId);
    }

    @PreAuthorize("@methodSecurityService.isOwnerOfBung(#userDetails, #bungId)")
    public void deleteBung(UserDetailsImpl userDetails, String bungId) {
        bungRepository.deleteByBungId(bungId);
    }

    public Page<BungInfoDto> getBungLists(UserDetailsImpl userDetails,
        boolean isAvailableOnly, Pageable pageable) {
        User user = userRepository.findByEmail(userDetails.getUser().getEmail());

        return bungRepository
            .findBungsWithStatus(user, isAvailableOnly, pageable)
            .map(BungInfoDto::new);
    }

    public Page<BungInfoDto> getMyBungLists(UserDetailsImpl userDetails,
        Boolean isOwned, BungStatus status, Pageable pageable) {
        User user = userRepository.findByEmail(userDetails.getUser().getEmail());

        return userBungRepository
            .findJoinedBungsByUserWithStatus(user, isOwned, status, pageable)
            .map(BungInfoDto::new);
    }
}
