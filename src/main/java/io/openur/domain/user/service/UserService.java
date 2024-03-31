package io.openur.domain.user.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
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

    public GetUserResponseDto getUserById(Long userId) {
        UserEntity userEntity = userRepository.findById(userId);
        return new GetUserResponseDto(userEntity);
    }

    public boolean existNickname(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }

    public GetUserResponseDto getUserEmail(String authorizationHeader){
        Claims claims = Jwts.parser().parseClaimsJws(authorizationHeader).getBody();

        // 클레임에서 이메일 추출
        String email = claims.getSubject();

        UserEntity userEntity = userRepository.findByEmail(email);
        return new GetUserResponseDto(userEntity);
    }

    @Transactional
    public void saveSurveyResult(Long userId, PatchUserSurveyRequestDto patchUserSurveyRequestDto) {
        User user = User.from(userRepository.findById(userId));

        user.update(patchUserSurveyRequestDto);
        userRepository.update(user);
    }
}
