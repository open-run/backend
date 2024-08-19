package io.openur.domain.user.service;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.user.dto.GetSelfResponseDto;
import io.openur.domain.user.dto.GetUsersResponseDto;
import io.openur.domain.user.dto.PatchUserSurveyRequestDto;
import io.openur.domain.user.model.User;
import io.openur.domain.user.repository.UserRepositoryImpl;
import io.openur.domain.userbung.repository.UserBungRepositoryImpl;
import io.openur.global.security.UserDetailsImpl;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepositoryImpl userRepository;
    private final UserBungRepositoryImpl userBungRepositoryImpl;

    public boolean existNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    public String getUserById(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        String email = userDetails.getUser().getEmail();
        User user = userRepository.findByEmail(email);
        return user.getUserId();
    }

    public GetSelfResponseDto getUserEmail(@AuthenticationPrincipal UserDetailsImpl userDetails){
        String email = userDetails.getUser().getEmail();
        User user = userRepository.findByEmail(email);

        return new GetSelfResponseDto(user);
    }

    public Page<GetUsersResponseDto> getMemberSuggestion(@AuthenticationPrincipal UserDetailsImpl userDetails,
        Pageable pageable) {
        List<Bung> bungs = userBungRepositoryImpl.findAllJoinBungsByUser(userDetails.getUser());

        return userBungRepositoryImpl.findAllFrequentUsers(bungs, userDetails.getUser(), pageable);
    }

    @Transactional
    public void deleteUserInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findByEmail(userDetails.getUser().getEmail());
        userRepository.deleteUserInfo(user);
    }

    @Transactional
    public void saveSurveyResult(@AuthenticationPrincipal UserDetailsImpl userDetails, PatchUserSurveyRequestDto patchUserSurveyRequestDto) {
        String email = userDetails.getUser().getEmail();
        User user = userRepository.findByEmail(email);
        user.update(patchUserSurveyRequestDto);
        userRepository.update(user);
    }
}
