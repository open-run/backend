package io.openur.domain.NFT.service;

import java.math.BigInteger;
import java.util.List;

public interface NftContractBalanceClient {

    List<BigInteger> getBatchBalances(String ownerAddress, List<BigInteger> tokenIds);

    BigInteger getBalance(String ownerAddress, BigInteger tokenId);
}
