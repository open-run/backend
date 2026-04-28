package io.openur.domain.NFT.service;

import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NftBalanceReaderImpl implements NftBalanceReader {

    private final NftContractBalanceClient nftContractBalanceClient;

    @Override
    public Map<String, BigInteger> getBalances(String ownerAddress, List<String> tokenIds) {
        if (tokenIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<BigInteger> parsedTokenIds = tokenIds.stream()
            .map(BigInteger::new)
            .toList();

        try {
            return toBalanceMap(tokenIds, nftContractBalanceClient.getBatchBalances(ownerAddress, parsedTokenIds));
        } catch (RuntimeException ignored) {
            return getBalancesOneByOne(ownerAddress, tokenIds, parsedTokenIds);
        }
    }

    private Map<String, BigInteger> toBalanceMap(List<String> tokenIds, List<BigInteger> balances) {
        if (tokenIds.size() != balances.size()) {
            throw new IllegalStateException("NFT balance response size mismatch");
        }

        Map<String, BigInteger> result = new LinkedHashMap<>();
        for (int i = 0; i < tokenIds.size(); i++) {
            result.put(tokenIds.get(i), balances.get(i));
        }
        return result;
    }

    private Map<String, BigInteger> getBalancesOneByOne(
        String ownerAddress,
        List<String> tokenIds,
        List<BigInteger> parsedTokenIds
    ) {
        Map<String, BigInteger> result = new LinkedHashMap<>();
        for (int i = 0; i < tokenIds.size(); i++) {
            result.put(
                tokenIds.get(i),
                nftContractBalanceClient.getBalance(ownerAddress, parsedTokenIds.get(i))
            );
        }
        return result;
    }
}
