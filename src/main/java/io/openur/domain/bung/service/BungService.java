package io.openur.domain.bung.service;

import static io.openur.global.common.UtilController.applyIfNotNull;

import io.openur.domain.bung.dto.BungInfoDto;
import io.openur.domain.bung.dto.BungInfoWithMemberListDto;
import io.openur.domain.bung.dto.BungInfoWithOwnershipDto;
import io.openur.domain.bung.dto.CreateBungDto;
import io.openur.domain.bung.dto.EditBungDto;
import io.openur.domain.bung.enums.CompleteBungResultEnum;
import io.openur.domain.bung.enums.EditBungResultEnum;
import io.openur.domain.bung.enums.GetBungResultEnum;
import io.openur.domain.bung.enums.JoinBungResultEnum;
import io.openur.domain.bung.enums.SearchBungTypeEnum;
import io.openur.domain.bung.exception.CompleteBungException;
import io.openur.domain.bung.exception.EditBungException;
import io.openur.domain.bung.exception.GetBungException;
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
import org.springframework.util.StringUtils;

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
        User user = userRepository.findUser(userDetails.getUser());
        Bung bung = bungRepository.save(new Bung(dto));
        userBungRepository.save(UserBung.isOwnerBung(user, bung));
        return bung;
    }

    private void saveHashtags(List<String> hashtagStrList) {
        List<Hashtag> hashtags = hashtagRepository.saveAll(hashtagStrList);
//        bungHashtagRepository.bulkInsertHashtags(bung, hashtags);
    }

    private void updateHashtags(Bung bung, List<String> hashtagStrList) {
        if(hashtagStrList.isEmpty()) {
            return;
        }
//         bung.toEntity()
        
        applyIfNotNull(hashtagStrList, hashtagsStr -> {
            List<Hashtag> hashtags = hashtagRepository.saveAll(hashtagsStr);
            bungHashtagRepository.updateHashtags(bung, hashtags);
        });
    }

    @Transactional
    public BungInfoDto createBung(@AuthenticationPrincipal UserDetailsImpl userDetails,
        CreateBungDto dto) {
        Bung bung = this.saveNewBung(userDetails, dto);
        this.saveHashtags(dto.getHashtags());
        return new BungInfoDto(bung, dto.getHashtags());
    }

    public BungInfoWithMemberListDto getBungDetail(String bungId) {
        return userBungRepository.findBungWithUsersById(bungId)
            .orElseThrow(() -> new GetBungException(GetBungResultEnum.BUNG_NOT_FOUND));
    }

    @Transactional
    @PreAuthorize("@methodSecurityService.isOwnerOfBung(#userDetails, #bungId)")
    public void deleteBung(UserDetailsImpl userDetails, String bungId) {
        userBungRepository.deleteByBungId(bungId);
        bungRepository.deleteByBungId(bungId);
    }
    
    public Page<BungInfoWithMemberListDto> getBungLists(
        UserDetailsImpl userDetails, SearchBungTypeEnum type, String keyword,
        boolean isJoinedOnly, Pageable pageable
    ) {
        if(SearchBungTypeEnum.needToSearch(type) && !StringUtils.hasText(keyword))
            throw new GetBungException(GetBungResultEnum.EMPTY_KEYWORD);
        
        User user = userRepository.findUser(userDetails.getUser());

        return switch (type) {
            case ALL -> bungRepository.findBungsWithStatus(user, isJoinedOnly, pageable);
            case MEMBER_NAME -> userBungRepository.findBungWithUserName(keyword, pageable);
            case HASHTAG ->  null; //bungRepository.findBungsWithHashtag(keyword, pageable);
            case LOCATION -> bungRepository.findBungsWithLocation(keyword, pageable);
        };
    }
    
    public Page<BungInfoWithOwnershipDto> getMyBungLists(
        UserDetailsImpl userDetails, Boolean isOwned, BungStatus status, Pageable pageable
    ) {
        User user = userRepository.findUser(userDetails.getUser());

        return userBungRepository
            .findJoinedBungsByUserWithStatus(user, isOwned, status, pageable)
            .map(BungInfoWithOwnershipDto::new);
    }
    // 전체, 멤버 해시태그
    
    @Transactional
    public JoinBungResultEnum joinBung(UserDetailsImpl userDetails, String bungId)
        throws JoinBungException {
        if (bungRepository.isBungStarted(bungId)) {
            throw new JoinBungException(JoinBungResultEnum.BUNG_HAS_ALREADY_STARTED);
        }

        BungInfoWithMemberListDto bungWithMembers = getBungDetail(bungId);
        if (bungWithMembers.getMemberList().stream().anyMatch(
                user -> user.getUserId().equals(userDetails.getUser().getUserId())
        )) {
            throw new JoinBungException(JoinBungResultEnum.USER_HAS_ALREADY_JOINED);
        }

        if (bungWithMembers.getMemberList().size() == bungWithMembers.getMemberNumber()) {
            throw new JoinBungException(JoinBungResultEnum.BUNG_IS_FULL);
        }

        userBungRepository.save(new UserBung(userDetails.getUser(), new Bung(bungWithMembers)));
        return JoinBungResultEnum.SUCCESSFULLY_JOINED;
    }
    
    @Transactional
    @PreAuthorize("@methodSecurityService.isOwnerOfBung(#userDetails, #bungId)")
    public EditBungResultEnum editBung(
        UserDetailsImpl userDetails, String bungId, EditBungDto editBungDto) {
        Bung bung = bungRepository.findBungById(bungId);

        if (bung.isCompleted()) {
            throw new EditBungException(EditBungResultEnum.BUNG_HAS_ALREADY_COMPLETED);
        }

        if (bung.getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new EditBungException(EditBungResultEnum.BUNG_HAS_ALREADY_STARTED);
        }
        
        if (editBungDto.getHasAfterRun() && !StringUtils.hasText(editBungDto.getAfterRunDescription())) {
            throw new EditBungException(EditBungResultEnum.BUNG_AFTER_RUN_DESCRIPTION_MISSING);
        }

        int numberOfCurrentMember = userBungRepository.countParticipantsByBungId(bungId);
        if (editBungDto.getMemberNumber() < numberOfCurrentMember) {
            throw new EditBungException(EditBungResultEnum.BUNG_PARTICIPANTS_EXCEEDED);
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
            throw new CompleteBungException(CompleteBungResultEnum.BUNG_HAS_ALREADY_COMPLETED);
        }

        if (bung.getStartDateTime().isAfter(LocalDateTime.now())) {
            throw new CompleteBungException(CompleteBungResultEnum.BUNG_HAS_NOT_STARTED);
        }

        //TODO: EventPublisher 로 도전과제 부가 기능 연산 필요, 도전과제에 따라 bung 이 가진 필드를 가져가는 DTO 가 필요할것
        List<String> memberIds = getBungDetail(bungId)
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
