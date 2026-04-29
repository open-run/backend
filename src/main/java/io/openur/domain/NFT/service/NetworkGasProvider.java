package io.openur.domain.NFT.service;

import io.openur.domain.NFT.exception.MintException;
import java.io.IOException;
import java.math.BigInteger;
import org.web3j.protocol.Web3j;
import org.web3j.tx.gas.ContractGasProvider;

public class NetworkGasProvider implements ContractGasProvider {

    private static final BigInteger DEFAULT_GAS_LIMIT = BigInteger.valueOf(4_300_000L);

    private final Web3j web3j;

    public NetworkGasProvider(Web3j web3j) {
        this.web3j = web3j;
    }

    @Override
    public BigInteger getGasPrice(String contractFunc) {
        return getGasPrice();
    }

    @Override
    public BigInteger getGasPrice() {
        try {
            return web3j.ethGasPrice().send().getGasPrice();
        } catch (IOException e) {
            throw new MintException("Failed to fetch gas price: " + e.getMessage());
        }
    }

    @Override
    public BigInteger getGasLimit(String contractFunc) {
        return DEFAULT_GAS_LIMIT;
    }

    @Override
    public BigInteger getGasLimit() {
        return DEFAULT_GAS_LIMIT;
    }
}
