package io.openur.domain.user.service;

import io.openur.domain.user.dto.GetUserResponseDto;
import io.openur.domain.user.dto.GetUsersResponseDto;
import io.openur.domain.user.dto.PatchUserSurveyRequestDto;
import io.openur.domain.user.model.User;
import io.openur.domain.user.repository.UserRepository;
import io.openur.domain.userbung.repository.UserBungRepository;
import io.openur.global.security.UserDetailsImpl;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserBungRepository userBungRepository;
    private final UserProfileImageStorageService userProfileImageStorageService;
    private final UserProfileImageUrlResolver userProfileImageUrlResolver;

    public String getUserById(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findUser(userDetails.getUser());
        return user.getUserId();
    }

    public boolean existNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    public GetUserResponseDto getUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findUser(userDetails.getUser());
        return new GetUserResponseDto(user, userProfileImageUrlResolver.resolve(user.getProfileImageStorageKey()));
    }

    public List<GetUserResponseDto> searchByNickname(String nickname) {
        List<User> users = userRepository.findByUserNickname(nickname);
        return users.stream()
            .map(user -> new GetUserResponseDto(
                user,
                userProfileImageUrlResolver.resolve(user.getProfileImageStorageKey())
            ))
            .toList();
    }

    public Page<GetUsersResponseDto> getUserSuggestion(
        @AuthenticationPrincipal UserDetailsImpl userDetails, Pageable pageable) {

        List<String> bungIds = userBungRepository.findJoinedBungsId(userDetails.getUser());

        return userBungRepository
            .findAllFrequentUsers(bungIds, userDetails.getUser(), pageable)
            .map(userCounts -> new GetUsersResponseDto(userCounts, userProfileImageUrlResolver));
    }

    @Transactional
    public GetUserResponseDto saveProfileImage(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        MultipartFile image
    ) {
        User user = userRepository.findUser(userDetails.getUser());
        String storageKey = userProfileImageStorageService.store(user.getUserId(), image);
        user.updateProfileImageStorageKey(storageKey);
        userRepository.update(user);

        return new GetUserResponseDto(user, userProfileImageUrlResolver.resolve(storageKey));
    }

    @Transactional
    public void deleteUserInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findUser(userDetails.getUser());
        userRepository.deleteUserInfo(user);
    }

    @Transactional
    public void saveSurveyResult(@AuthenticationPrincipal UserDetailsImpl userDetails,
        PatchUserSurveyRequestDto patchUserSurveyRequestDto) {
        User user = userRepository.findUser(userDetails.getUser());
        user.update(patchUserSurveyRequestDto);
        userRepository.update(user);
    }

    @Transactional
    @PreAuthorize("@methodSecurityService.isBungParticipant(#userDetails, #bungId)")
    public List<String> increaseFeedback(UserDetailsImpl userDetails, String bungId, List<String> targetUserIds
    ) {
        if (targetUserIds == null || targetUserIds.isEmpty()) {
            throw new IllegalArgumentException("Target user IDs cannot be null or empty");
        }

        return userRepository.batchIncrementFeedback(targetUserIds);
    }
}
