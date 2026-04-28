package io.openur.domain.NFT.service;

import java.math.BigInteger;

public interface NftMintClient {

    String mintToken(String recipientAddress, BigInteger tokenId, BigInteger amount);
}
