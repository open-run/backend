package io.openur.domain.user.service;

import io.openur.domain.user.dto.GetUserResponseDto;
import io.openur.domain.user.dto.PatchUserSurveyRequestDto;
import io.openur.domain.user.entity.UserEntity;
import io.openur.domain.user.model.User;
import io.openur.domain.user.repository.UserRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepositoryImpl userRepository;

    public GetUserResponseDto getUserById(String userId) {
        UserEntity userEntity = userRepository.findById(userId);
        return new GetUserResponseDto(userEntity);
    }

    public boolean existNickname(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }

    @Transactional
    public void saveSurveyResult(String userId, PatchUserSurveyRequestDto patchUserSurveyRequestDto) {
        User user = User.from(userRepository.findById(userId));

        user.update(patchUserSurveyRequestDto);
        userRepository.update(user);
    }
}
