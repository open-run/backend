package io.openur.domain.NFT.service;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public interface NftBalanceReader {

    Map<String, BigInteger> getBalances(String ownerAddress, List<String> tokenIds);
}
