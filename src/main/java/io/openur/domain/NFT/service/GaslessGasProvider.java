package io.openur.domain.NFT.service;

import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;

public class GaslessGasProvider implements ContractGasProvider {
    
    private static final BigInteger ZERO_GAS_PRICE = BigInteger.ZERO;
    private static final BigInteger DEFAULT_GAS_LIMIT = BigInteger.valueOf(4_300_000L);

    @Override
    public BigInteger getGasPrice(String contractFunc) {
        return ZERO_GAS_PRICE;
    }

    @Override
    public BigInteger getGasPrice() {
        return ZERO_GAS_PRICE;
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
