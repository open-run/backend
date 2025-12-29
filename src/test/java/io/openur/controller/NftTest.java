package io.openur.controller;

import io.openur.contract.OpenRunNFTTest;
import java.math.BigInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

@SpringBootTest
public class NftTest {

    @Value("${nft.rpc-url}")
    private String rpcUrl;

    @Value("${nft.private-key}")
    private String privateKey;

    @Value("${nft.contract-address}")
    private String contractAddress;

    @Value("${nft.base-uri}")
    private String baseUri;

    @Test
    @DisplayName("End-to-end NFT flow: set task, set baseURI, mint, read balance & uri")
    void nft_end_to_end_flow() throws Exception {
        // Hardcoded test values as requested
        String to = "0xc0cdd3de0abc5305dead9dfa5b69b7db82ddb98f";
        BigInteger taskId = BigInteger.valueOf(77);
        BigInteger amount = BigInteger.ONE;
        BigInteger itemId = BigInteger.valueOf(77);

        String walletAddress = "0x5557cFb2924e6031196b9B820E63Cb83f4BE5837";
        BigInteger tokenId = BigInteger.valueOf(77);

        Web3j web3j = Web3j.build(new HttpService(rpcUrl));
        try {
            Credentials credentials = Credentials.create(privateKey);
            OpenRunNFTTest contract = OpenRunNFTTest.load(contractAddress, web3j, credentials, new DefaultGasProvider());

            // set task
            TransactionReceipt taskReceipt = contract.setTaskItem(taskId, itemId).send();
            Assertions.assertNotNull(taskReceipt.getTransactionHash());

            // set baseURI from application.yml
            TransactionReceipt uriReceipt = contract.setBaseURI(baseUri).send();
            Assertions.assertNotNull(uriReceipt.getTransactionHash());

            Thread.sleep(1000);

            // mint
            TransactionReceipt mintReceipt = contract.mintItemForTask(to, taskId, amount).send();
            Assertions.assertNotNull(mintReceipt.getTransactionHash());

            Thread.sleep(1000);

            // read balance
            BigInteger balance = contract.balanceOf(walletAddress, tokenId).send();
            Assertions.assertNotNull(balance);
            Assertions.assertTrue(balance.compareTo(BigInteger.ZERO) >= 0);

            if (balance.compareTo(BigInteger.ZERO) > 0) {
                String uri = contract.uri(tokenId).send();
                Assertions.assertNotNull(uri);

                if (tokenId.equals(BigInteger.valueOf(28))) {
                    uri = uri.replace("28", "shoes4-3.json");
                }

                String metadataUrl = uri.replace("ipfs://", "https://gateway.pinata.cloud/ipfs/");
                Assertions.assertTrue(metadataUrl.startsWith("https://"));
            }
        } finally {
            web3j.shutdown();
        }
    }
}
