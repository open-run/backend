package io.openur.domain.bung.service;

import io.openur.domain.bung.dto.BungInfoDto;
import io.openur.domain.bung.dto.BungInfoWithMemberListDto;
import io.openur.domain.bung.dto.BungInfoWithOwnershipDto;
import io.openur.domain.bung.dto.CreateBungDto;
import io.openur.domain.bung.dto.EditBungDto;
import io.openur.domain.bung.enums.BungStatus;
import io.openur.domain.bung.enums.CompleteBungResultEnum;
import io.openur.domain.bung.enums.EditBungResultEnum;
import io.openur.domain.bung.enums.GetBungResultEnum;
import io.openur.domain.bung.enums.JoinBungResultEnum;
import io.openur.domain.bung.enums.SearchBungResultEnum;
import io.openur.domain.bung.exception.CompleteBungException;
import io.openur.domain.bung.exception.EditBungException;
import io.openur.domain.bung.exception.GetBungException;
import io.openur.domain.bung.exception.JoinBungException;
import io.openur.domain.bung.exception.SearchBungException;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.bung.repository.BungRepository;
import io.openur.domain.bunghashtag.repository.BungHashtagRepository;
import io.openur.domain.challenge.event.ChallengeEventsPublisher;
import io.openur.domain.hashtag.model.Hashtag;
import io.openur.domain.hashtag.repository.HashtagRepository;
import io.openur.domain.user.model.User;
import io.openur.domain.user.repository.UserRepository;
import io.openur.domain.userbung.dto.UserBungInfoDto;
import io.openur.domain.userbung.model.UserBung;
import io.openur.domain.userbung.repository.UserBungRepository;
import io.openur.global.security.UserDetailsImpl;
import java.time.LocalDateTime;
import java.util.Collections;
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

    private final BungRepository bungRepository;
    private final UserBungRepository userBungRepository;
    private final UserRepository userRepository;
    private final HashtagRepository hashtagRepository;
    private final BungHashtagRepository bungHashtagRepository;
    private final ChallengeEventsPublisher challengeEventsPublisher;
    
    @Transactional
    public BungInfoDto createBung(
        @AuthenticationPrincipal UserDetailsImpl userDetails, CreateBungDto dto)
    {
        List<Hashtag> hashtags = hashtagRepository.saveNotListedTags(dto.getHashtags());
        Bung bung = this.saveNewBung(userDetails, dto, hashtags);
        
        return new BungInfoDto(bung);
    }
    
    private Bung saveNewBung(
        UserDetailsImpl userDetails, CreateBungDto dto, List<Hashtag> hashtags
    ) {
        User user = userRepository.findUser(userDetails.getUser());
        Bung bung = bungRepository.save(new Bung(dto), Collections.emptyList());
        
        bung = bungHashtagRepository.saveNewBungHashtag(bung, hashtags);
        userBungRepository.save(UserBung.isOwnerBung(user, bung));
        return bung;
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
        UserDetailsImpl userDetails,
        Pageable pageable
    ) {
        User user = userRepository.findUser(userDetails.getUser());

        return bungRepository.findBungsWithStatus(user, pageable);
    }
    
    public Page<BungInfoWithOwnershipDto> getMyBungLists(
        UserDetailsImpl userDetails, Boolean isOwned, BungStatus status, Pageable pageable
    ) {
        User user = userRepository.findUser(userDetails.getUser());

        return userBungRepository
            .findJoinedBungsByUserWithStatus(user, isOwned, status, pageable)
            .map(BungInfoWithOwnershipDto::new);
    }

    public Page<BungInfoDto> searchBungByLocation(
        UserDetailsImpl userDetails, String location, Pageable pageable
    ) {
        if(!StringUtils.hasText(location))
            throw new SearchBungException(SearchBungResultEnum.NO_LOCATION_SPECIFIED);

        return bungRepository.findBungsWithLocation(location, pageable);
    }

    public Page<BungInfoWithMemberListDto> searchBungByNickname(
        UserDetailsImpl userDetails, String nickname, Pageable pageable
    ) {
        if(!StringUtils.hasText(nickname))
            throw new SearchBungException(SearchBungResultEnum.NO_NICKNAME_PROVIDED);

        return userBungRepository.findBungsWithUserName(nickname, pageable);
    }

    public Page<BungInfoDto> searchBungByHashtag(
        UserDetailsImpl userDetails, List<String> hashtag, Pageable pageable
    ) {
        if(hashtag == null || hashtag.isEmpty())
            throw new SearchBungException(SearchBungResultEnum.NO_HASHTAG_PROVIDED);

        return bungRepository.findBungWithHashtag(hashtag, pageable);
    }

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
        UserDetailsImpl userDetails, String bungId, EditBungDto editBungDto
    ) {
        Bung bung = bungRepository.findBungById(bungId);

        if (bung.isCompleted()) { // 완료된 벙은 수정할 수 없다
            throw new EditBungException(EditBungResultEnum.BUNG_HAS_ALREADY_COMPLETED);
        }
        
        // 이미 수행 중인 벙 이벤트 또한 수정할 수 없다
        if (bung.getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new EditBungException(EditBungResultEnum.BUNG_HAS_ALREADY_STARTED);
        }
        
        // 수정시, 뒤풀이가 있는데 설명을 적지 않는 것은 허용하지 않는다
        if (editBungDto.getHasAfterRun() && !StringUtils.hasText(editBungDto.getAfterRunDescription())) {
            throw new EditBungException(EditBungResultEnum.BUNG_AFTER_RUN_DESCRIPTION_MISSING);
        }
        
        // 수정시, 이미 함께하고 있는 인원보다 적게 수정할 수는 없다
        int numberOfCurrentMember = userBungRepository.countParticipantsByBungId(bungId);
        if (editBungDto.getMemberNumber() < numberOfCurrentMember) {
            throw new EditBungException(EditBungResultEnum.BUNG_PARTICIPANTS_EXCEEDED);
        }

        bung.update(editBungDto);
        bung = bungRepository.save(bung, Collections.emptyList());
        
        this.updateHashtagConnection(bung, editBungDto.getHashtags());
        return EditBungResultEnum.SUCCESSFULLY_EDITED;
    }

    private void updateHashtagConnection(Bung bung, List<String> hashtagStrList) {
        // 변경 요청 해시태그가 공백인데, 원래도 공백이면 할거 없고
        if(hashtagStrList.isEmpty()) {
            if(bung.getHashtags().isEmpty()) return;
            
            bungHashtagRepository.deleteByBungId(bung.getBungId());
            return;
        }
        
        // hashtag 문자열에 해당하는 hashtag 비 영속성 객체로 얻어옴 ( 생성을 하던지 간에 )
        List<Hashtag> hashtags = hashtagRepository.saveNotListedTags(hashtagStrList);
        bungHashtagRepository.insertHashtagConnection(bung, hashtags);
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
        bungRepository.save(bung, Collections.emptyList());
        return CompleteBungResultEnum.SUCCESSFULLY_COMPLETED;
    }
}
