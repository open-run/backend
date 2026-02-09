package io.openur.global.common.scheduler;

import io.openur.domain.bung.model.Bung;
import io.openur.domain.bung.repository.BungRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BungScheduler {

    private final BungRepository bungRepository;

    @Scheduled(cron = "0 */1 * * * *")
    public void checkBungsPastStartWithLowParticipation() {
        Pageable pageable = PageRequest.of(0, 100);

        Page<Bung> bungs;

        do {
            bungs = bungRepository.findBungsPastStartWithSingleParticipant(
                pageable
            );
            if (bungs.isEmpty()) {
                return;
            }

            bungRepository.setAsFaded(bungs.getContent());
            pageable = pageable.next();


        } while (bungs.hasNext());
    }
}
