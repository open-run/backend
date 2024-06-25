package io.openur.domain.user.service;
import io.openur.domain.user.dto.GetUserResponseDto;
import io.openur.domain.user.dto.PatchUserSurveyRequestDto;
import io.openur.domain.user.model.User;
import io.openur.domain.user.repository.UserRepositoryImpl;
import io.openur.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepositoryImpl userRepository;

    public String getUserById(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        String email = userDetails.getUser().getEmail();
        User user = userRepository.findByEmail(email);
        return user.getUserId();
    }

    public boolean existNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    public GetUserResponseDto getUserEmail(@AuthenticationPrincipal UserDetailsImpl userDetails){
        String email = userDetails.getUser().getEmail();
        User user = userRepository.findByEmail(email);
        return new GetUserResponseDto(user);
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
