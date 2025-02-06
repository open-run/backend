package io.openur.domain.challenge.event;

import io.openur.domain.userchallenge.model.UserChallenge;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class OnWearing {

    private final List<UserChallenge> userChallenges;
    private final String wearingId; // TODO: NFT 정의할 떄 같이 정의 필요
}
