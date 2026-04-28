package io.openur.domain.NFT.service;

import io.openur.domain.NFT.contract.NFTContract;
import io.openur.domain.NFT.exception.MintException;
import jakarta.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;

@Service
@RequiredArgsConstructor
public class Web3jNftContractBalanceClient implements NftContractBalanceClient {

    private NFTContract nftContract;

    @Value("${nft.rpc-url:https://public.sepolia.rpc.status.network}")
    private String rpcUrl;

    @Value("${nft.private-key}")
    private String privateKey;

    @Value("${nft.contract-address}")
    private String contractAddress;

    @Value("${nft.chain-id:1660990954}")
    private Long chainId;

    @PostConstruct
    public void init() {
        Web3j web3j = Web3j.build(new HttpService(rpcUrl));
        Credentials credentials = Credentials.create(privateKey);
        TransactionManager transactionManager = new RawTransactionManager(web3j, credentials, chainId);
        this.nftContract = NFTContract.load(contractAddress, web3j, transactionManager, new GaslessGasProvider());
    }

    @Override
    public List<BigInteger> getBatchBalances(String ownerAddress, List<BigInteger> tokenIds) {
        try {
            return nftContract.balanceOfBatch(ownerAddress, tokenIds).send();
        } catch (Exception e) {
            throw new MintException("Failed to get NFT batch balances: " + e.getMessage());
        }
    }

    @Override
    public BigInteger getBalance(String ownerAddress, BigInteger tokenId) {
        try {
            return nftContract.balanceOf(ownerAddress, tokenId).send();
        } catch (Exception e) {
            throw new MintException("Failed to get NFT balance: " + e.getMessage());
        }
    }
}
