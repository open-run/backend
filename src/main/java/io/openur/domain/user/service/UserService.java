package io.openur.domain.user.service;
import io.jsonwebtoken.Claims;
import io.openur.domain.user.dto.GetUserResponseDto;
import io.openur.domain.user.dto.PatchUserSurveyRequestDto;
import io.openur.domain.user.entity.UserEntity;
import io.openur.domain.user.model.User;
import io.openur.domain.user.repository.UserRepositoryImpl;
import io.openur.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepositoryImpl userRepository;
    private final JwtUtil jwtUtil;

    public String getUserById(String jwtToken) {
        Claims claims = jwtUtil.getUserInfoFromToken(jwtToken);
        // 클레임에서 이메일 추출
        String email = claims.getSubject();
        UserEntity userEntity = userRepository.findByEmail(email);

        return userEntity.getUserId();
    }

    public boolean existNickname(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }

    public GetUserResponseDto getUserEmail(String jwtToken){
        Claims claims = jwtUtil.getUserInfoFromToken(jwtToken);
        // 클레임에서 이메일 추출
        String email = claims.getSubject();
        UserEntity userEntity = userRepository.findByEmail(email);
        return new GetUserResponseDto(userEntity);
    }

    @Transactional
    public void saveSurveyResult(String jwtToken, PatchUserSurveyRequestDto patchUserSurveyRequestDto) {
        Claims claims = jwtUtil.getUserInfoFromToken(jwtToken);
        // 클레임에서 이메일 추출
        String email = claims.getSubject();
        User user = User.from(userRepository.findByEmail(email));
        user.update(patchUserSurveyRequestDto);
        userRepository.update(user);
    }
}
