package io.openur.domain.NFT.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NftMintJobAsyncExecutor {

    private final NftMintJobProcessor nftMintJobProcessor;

    @Async
    public void processAsync(Long mintJobId) {
        nftMintJobProcessor.process(mintJobId);
    }
}
