package io.openur.domain.user.service;

import io.openur.domain.user.dto.GetUserResponseDto;
import io.openur.domain.user.entity.UserEntity;
import io.openur.domain.user.repository.UserRepositoryImpl;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepositoryImpl userRepository;

    public GetUserResponseDto getUserById(Long userId) {
        UserEntity userEntity = userRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException("User not found"));
        return new GetUserResponseDto(userEntity);
    }



    public boolean existNickname(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }
}
