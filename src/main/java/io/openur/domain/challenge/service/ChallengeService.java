package io.openur.domain.challenge.service;

import io.openur.domain.challenge.dto.ChallengeInfoDto;
import io.openur.domain.challenge.model.CompletedType;
import io.openur.domain.user.model.User;
import io.openur.domain.user.repository.UserRepository;
import io.openur.domain.userchallenge.repository.UserChallengeRepository;
import io.openur.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeService {
    private final UserRepository userRepository;
    private final UserChallengeRepository userChallengeRepository;

    public Page<ChallengeInfoDto> getMyChallengeList(
        UserDetailsImpl userDetails, CompletedType type, Pageable pageable
    ) {
        User user = userRepository.findUser(userDetails.getUser());

        return userChallengeRepository.findByUserIdAndChallengeType(
            user.getUserId(), type, pageable
            ).map(ChallengeInfoDto::new);
    }
}
