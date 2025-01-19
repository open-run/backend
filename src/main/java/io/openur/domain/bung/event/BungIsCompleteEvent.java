package io.openur.domain.bung.event;

import io.openur.domain.bung.model.Bung;
import io.openur.domain.challenge.ChallengeEvent;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;

@Getter
public class BungIsCompleteEvent extends ChallengeEvent {

    private static final List<Long> BUNG_IS_COMPLETE_CHALLENGE_IDS = Arrays.asList(
        1L,  // Tutorial: Complete bung 1 time
        1001L // Normal: Complete bung 5 times
        // TODO Add more challenge IDs that check for bung completion
    );

    private final Bung bung;
    private final List<String> userIds;

    public BungIsCompleteEvent(Object source, Bung bung, List<String> userIds) {
        super(source, BUNG_IS_COMPLETE_CHALLENGE_IDS);
        this.bung = bung;
        this.userIds = userIds;
    }
}
