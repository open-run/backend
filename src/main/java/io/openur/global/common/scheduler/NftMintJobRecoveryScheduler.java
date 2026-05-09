package io.openur.global.common.scheduler;

import io.openur.domain.NFT.entity.NftMintJobEntity;
import io.openur.domain.NFT.enums.NftMintJobStatus;
import io.openur.domain.NFT.repository.NftMintJobJpaRepository;
import io.openur.domain.NFT.service.NftMintJobAsyncExecutor;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NftMintJobRecoveryScheduler {

    private static final Duration PENDING_STUCK_THRESHOLD = Duration.ofMinutes(5);
    private static final Duration MINTING_STUCK_THRESHOLD = Duration.ofMinutes(15);
    private static final int BATCH_SIZE = 100;

    private final NftMintJobJpaRepository nftMintJobJpaRepository;
    private final NftMintJobAsyncExecutor nftMintJobAsyncExecutor;

    @Scheduled(cron = "0 */5 * * * *")
    public void recoverStuckJobs() {
        LocalDateTime now = LocalDateTime.now();

        forEachStuckBatch(NftMintJobStatus.PENDING, now.minus(PENDING_STUCK_THRESHOLD), batch -> {
            for (NftMintJobEntity job : batch) {
                log.warn("Recovering stuck PENDING mint job: {}", job.getMintJobId());
                nftMintJobAsyncExecutor.processAsync(job.getMintJobId());
            }
        });

        forEachStuckBatch(NftMintJobStatus.MINTING, now.minus(MINTING_STUCK_THRESHOLD), batch ->
            log.error("Stuck MINTING jobs requiring manual review: {}",
                batch.stream().map(NftMintJobEntity::getMintJobId).toList())
        );
    }

    private void forEachStuckBatch(
        NftMintJobStatus status,
        LocalDateTime cutoff,
        Consumer<List<NftMintJobEntity>> batchHandler
    ) {
        Pageable pageable = PageRequest.of(0, BATCH_SIZE);
        Slice<NftMintJobEntity> page;
        do {
            page = nftMintJobJpaRepository.findByStatusAndUpdatedAtBefore(status, cutoff, pageable);
            if (page.isEmpty()) {
                return;
            }
            batchHandler.accept(page.getContent());
            pageable = pageable.next();
        } while (page.hasNext());
    }
}
