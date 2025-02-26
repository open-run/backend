package io.openur.domain.bung.service;

import static io.openur.global.common.UtilController.applyIfNotNull;

import io.openur.domain.bung.dto.BungInfoDto;
import io.openur.domain.bung.dto.BungInfoWithMemberListDto;
import io.openur.domain.bung.dto.BungInfoWithOwnershipDto;
import io.openur.domain.bung.dto.CreateBungDto;
import io.openur.domain.bung.dto.EditBungDto;
import io.openur.domain.bung.enums.CompleteBungResultEnum;
import io.openur.domain.bung.enums.EditBungResultEnum;
import io.openur.domain.bung.enums.JoinBungResultEnum;
import io.openur.domain.bung.exception.CompleteBungException;
import io.openur.domain.bung.exception.EditBungException;
import io.openur.domain.bung.exception.JoinBungException;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.bung.model.BungStatus;
import io.openur.domain.bung.repository.BungRepositoryImpl;
import io.openur.domain.bunghashtag.repository.BungHashtagRepositoryImpl;
import io.openur.domain.challenge.event.ChallengeEventsPublisher;
import io.openur.domain.hashtag.model.Hashtag;
import io.openur.domain.hashtag.repository.HashtagRepositoryImpl;
import io.openur.domain.user.model.User;
import io.openur.domain.user.repository.UserRepositoryImpl;
import io.openur.domain.userbung.dto.UserBungInfoDto;
import io.openur.domain.userbung.model.UserBung;
import io.openur.domain.userbung.repository.UserBungRepositoryImpl;
import io.openur.global.security.UserDetailsImpl;
import java.time.LocalDateTime;
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
    private final ChallengeEventsPublisher challengeEventsPublisher;

    private Bung saveNewBung(UserDetailsImpl userDetails, CreateBungDto dto) {
        User user = userRepository.findByEmail(userDetails.getUser().getEmail());
        Bung bung = bungRepository.save(new Bung(dto));
        userBungRepository.save(UserBung.isOwnerBung(user, bung));
        return bung;
    }

    private void saveHashtags(Bung bung, List<String> hashtagStrList) {
        List<Hashtag> hashtags = hashtagRepository.saveAll(hashtagStrList);
        bungHashtagRepository.bulkInsertHashtags(bung, hashtags);
    }

    private void updateHashtags(Bung bung, List<String> hashtagStrList) {
        applyIfNotNull(hashtagStrList, hashtagsStr -> {
            List<Hashtag> hashtags = hashtagRepository.saveAll(hashtagsStr);
            bungHashtagRepository.updateHashtags(bung, hashtags);
        });
    }

    @Transactional
    public BungInfoDto createBung(@AuthenticationPrincipal UserDetailsImpl userDetails,
        CreateBungDto dto) {
        Bung bung = this.saveNewBung(userDetails, dto);
        this.saveHashtags(bung, dto.getHashtags());
        return new BungInfoDto(bung, dto.getHashtags());
    }

    public BungInfoWithMemberListDto getBungDetail(String bungId) {
        return userBungRepository.findBungWithUsersById(bungId);
    }

    @Transactional
    @PreAuthorize("@methodSecurityService.isOwnerOfBung(#userDetails, #bungId)")
    public void deleteBung(UserDetailsImpl userDetails, String bungId) {
        userBungRepository.deleteByBungId(bungId);
        bungRepository.deleteByBungId(bungId);
    }

    public Page<BungInfoWithMemberListDto> getBungLists(
        UserDetailsImpl userDetails,
        boolean isAvailableOnly,
        Pageable pageable
    ) {
        User user = userRepository.findByEmail(userDetails.getUser().getEmail());

        return bungRepository
            .findBungsWithStatus(user, isAvailableOnly, pageable);
    }

    public Page<BungInfoWithOwnershipDto> getMyBungLists(
        UserDetailsImpl userDetails,
        Boolean isOwned,
        BungStatus status,
        Pageable pageable
    ) {
        User user = userRepository.findByEmail(userDetails.getUser().getEmail());

        return userBungRepository
            .findJoinedBungsByUserWithStatus(user, isOwned, status, pageable)
            .map(BungInfoWithOwnershipDto::new);
    }

    @Transactional
    public JoinBungResultEnum joinBung(UserDetailsImpl userDetails, String bungId) throws JoinBungException {
        if (bungRepository.isBungStarted(bungId)) {
            throw new JoinBungException(JoinBungResultEnum.BUNG_HAS_ALREADY_STARTED.toString());
        }

        BungInfoWithMemberListDto bungWithMembers = userBungRepository.findBungWithUsersById(
            bungId);
        if (bungWithMembers.getMemberList().stream().anyMatch(
                user -> user.getUserId().equals(userDetails.getUser().getUserId())
        )) {
            throw new JoinBungException(JoinBungResultEnum.USER_HAS_ALREADY_JOINED.toString());
        }

        if (bungWithMembers.getMemberList().size() == bungWithMembers.getMemberNumber()) {
            throw new JoinBungException(JoinBungResultEnum.BUNG_IS_FULL.toString());
        }

        userBungRepository.save(new UserBung(userDetails.getUser(), new Bung(bungWithMembers)));
        return JoinBungResultEnum.SUCCESSFULLY_JOINED;
    }

    @Transactional
    @PreAuthorize("@methodSecurityService.isOwnerOfBung(#userDetails, #bungId)")
    public EditBungResultEnum editBung(UserDetailsImpl userDetails, String bungId, EditBungDto editBungDto) {
        Bung bung = bungRepository.findBungById(bungId);

        if (bung.isCompleted()) {
            throw new EditBungException(EditBungResultEnum.BUNG_HAS_ALREADY_COMPLETED.toString());
        }

        if (bung.getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new EditBungException(EditBungResultEnum.BUNG_HAS_ALREADY_STARTED.toString());
        }

        //프론트 요청 사항으로 return으로 result 값 0이 아닌 다른 값으로 보내고 메시지도 달라고 했음.
        int numberOfCurrentMember = userBungRepository.countParticipantsByBungId(bungId);
        if(editBungDto.getMemberNumber() < numberOfCurrentMember) {
            throw new EditBungException(EditBungResultEnum.BUNG_PARTICIPANTS_EXCEEDED.toString());
        }

        bung.update(editBungDto);
        updateHashtags(bung, editBungDto.getHashtags());
        bungRepository.save(bung);
        return EditBungResultEnum.SUCCESSFULLY_EDITED;
    }

    @Transactional
    @PreAuthorize("@methodSecurityService.isOwnerOfBung(#userDetails, #bungId)")
    public CompleteBungResultEnum completeBung(
        UserDetailsImpl userDetails,
        String bungId
    ) throws CompleteBungException {
        Bung bung = bungRepository.findBungById(bungId);
        if (bung.isCompleted()) {
            throw new CompleteBungException(CompleteBungResultEnum.BUNG_HAS_ALREADY_COMPLETED.toString());
        }

        if (bung.getStartDateTime().isAfter(LocalDateTime.now())) {
            throw new CompleteBungException(CompleteBungResultEnum.BUNG_HAS_NOT_STARTED.toString());
        }

        //TODO: EventPublisher 로 도전과제 부가 기능 연산 필요, 도전과제에 따라 bung 이 가진 필드를 가져가는 DTO 가 필요할것
        List<String> memberIds = userBungRepository.findBungWithUsersById(bungId)
            .getMemberList()
            .stream()
            .map(UserBungInfoDto::getUserId)
            .toList();
        challengeEventsPublisher.bungIsComplete(bung, memberIds);

        bung.completeBung();
        bungRepository.save(bung);
        return CompleteBungResultEnum.SUCCESSFULLY_COMPLETED;
    }
}
