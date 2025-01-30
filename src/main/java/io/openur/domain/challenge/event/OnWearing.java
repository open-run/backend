package io.openur.domain.challenge.event;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OnWearing {
    private final List<String> userIds;
    private final String wearingId; // TODO: NFT 정의할 떄 같이 정의 필요
}
