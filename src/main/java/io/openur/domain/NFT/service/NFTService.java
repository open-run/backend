package io.openur.domain.NFT.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.openur.domain.NFT.contract.NFTContract;
import io.openur.domain.NFT.dto.NFTMetadataDto;
import io.openur.domain.NFT.exception.MintException;
import io.openur.domain.challenge.model.Challenge;
import io.openur.domain.challenge.repository.ChallengeRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;

import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class NFTService {

    private NFTContract nftContract;
    private final ChallengeRepository challengeRepository;

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

    public Page<NFTMetadataDto> getNFTsByUserAddress(String userAddress, Pageable pageable) {
        // TODO: 해당 userAddress 가 보유하고 있는 모든 OpenRun NFT 를 조회하기
        return Page.empty();
    }


    public void setBaseURI(String baseURI) {
        try {
            TransactionReceipt uriReceipt = nftContract.setBaseURI(baseURI).send();
            if (!uriReceipt.isStatusOK()) {
                throw new MintException("setBaseURI status not OK: " + uriReceipt.getTransactionHash());
            }
            System.out.println("baseURI 설정 완료: " + uriReceipt.getTransactionHash());
        } catch (Exception e) {
            throw new MintException("Failed to set baseURI: " + e.getMessage());
        }
    }

    private BigInteger stringToTokenId(String string) {
        byte[] encoded = string.getBytes(StandardCharsets.UTF_8);
        byte[] hash = Hash.sha3(encoded);
        return new BigInteger(1, hash);
    }

    public NFTMetadataDto mintNFT(String userAddress, Long challengeId) {
        // TODO: challengeId를 통해 어떤 NFT를 민팅할지 결정하는 로직 필요
        Challenge challenge = challengeRepository.findById(challengeId);

        // 현재는 test를 위해 shoes1의 tokenId로 고정해둠
        BigInteger amount = BigInteger.ONE;
        BigInteger tokenId = this.stringToTokenId("shoes1");
        try {
            TransactionReceipt receipt = nftContract.mintToken(userAddress, tokenId, amount).send();
            if (!receipt.isStatusOK()) {
                throw new MintException("mintToken status not OK: " + receipt.getTransactionHash());
            }
            System.out.println("NFT minted successfully: " + receipt.getTransactionHash());
        } catch (Exception e) {
            throw new MintException("Failed to mint NFT: " + e.getMessage());
        }

        return getNftMetadata(tokenId);
    }

    private NFTMetadataDto getNftMetadata(BigInteger tokenId) {
        String tokenUri;
        try {
            tokenUri = nftContract.uri(tokenId).send();
        } catch (Exception e) {
            throw new MintException("Error from getting token URI: " + e.getMessage());
        }
        // tokenUri가 ipfs:// 형식일 경우 gateway로 변환
        String resolvedUri = tokenUri.replace("ipfs://", "https://gateway.pinata.cloud/ipfs/");

        // JSON 파싱
        URL url;
        try {
            url = new URL(resolvedUri);
        } catch (MalformedURLException e) {
            throw new MintException("Invalid URL: " + resolvedUri + "\n" + e.getMessage());
        }
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(url);
        } catch (IOException e) {
            throw new MintException("Error parsing JSON: " + e.getMessage());
        }

        String name = rootNode.path("name").asText();
        String description = rootNode.path("description").asText();
        String image = rootNode.path("image").asText();
        String category = rootNode.path("attributes").get(0).path("value").asText();
        String rarity = rootNode.path("attributes").get(1).path("value").asText();

        return NFTMetadataDto.builder()
            .tokenId(tokenId)
            .name(name)
            .description(description)
            .image(image)
            .category(category)
            .rarity(rarity)
            .build();
    }
}
