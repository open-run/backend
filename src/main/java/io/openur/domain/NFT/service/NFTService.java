package io.openur.domain.NFT.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.openur.domain.NFT.contract.NFTContract;
import io.openur.domain.NFT.dto.NFTMetadataDto;
import io.openur.domain.NFT.exception.MintException;
import io.openur.domain.challenge.model.Challenge;
import io.openur.domain.challenge.repository.ChallengeRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;

import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class NFTService {

    private NFTContract nftContract;
    private ChallengeRepository challengeRepository;

    @Value("${nft.base-uri}")
    private String baseUri;
    @Value("${nft.rpc-url}")
    private String rpcUrl;
    @Value("${nft.private-key}")
    private String privateKey;
    @Value("${nft.contract-address}")
    private String contractAddress;

    public NFTService(ChallengeRepository challengeRepository) {
        Web3j web3j = Web3j.build(new HttpService(rpcUrl));
        Credentials credentials = Credentials.create(privateKey);
        NFTContract nftContract = NFTContract.load(contractAddress, web3j, credentials, new DefaultGasProvider());
        this.nftContract = nftContract;
        this.challengeRepository = challengeRepository;
    }

    public Page<NFTMetadataDto> getNFTsByUserAddress(String userAddress, Pageable pageable) {
        // TODO: 해당 userAddress 가 보유하고 있는 모든 OpenRun NFT 를 조회하기
        return Page.empty();
    }

    public NFTMetadataDto mintNFT(String userAddress, Long challengeId) {
        // TODO: 현재 itemId 확인 후 다음 id 를 부여하는 로직으로 교체 필요.
        //현재는 test를 위해 77로 고정해둠
        BigInteger taskId = BigInteger.valueOf(77);
        BigInteger amount = BigInteger.ONE;
        BigInteger itemId = BigInteger.valueOf(77);

        // TODO: challengeId를 통해 어떤 NFT를 민팅할지 결정하는 로직 필요
        Challenge challenge = challengeRepository.findById(challengeId);
        
        try {
            TransactionReceipt taskReceipt = nftContract.setTaskItem(taskId, itemId).send();
            if (!taskReceipt.isStatusOK()) {
                throw new MintException("setTask status not OK: " + taskReceipt.getTransactionHash());
            }
            System.out.println("taskId " + taskId + "에 itemId " + itemId + " 등록 완료");
        } catch (Exception e) {
            throw new MintException("Failed to set task item: " + e.getMessage());
        }

        // TOCHECK: baseURI 를 민팅 할 때 마다 다시 설정해야 하는지 확인 필요.
        try {
            TransactionReceipt uriReceipt = nftContract.setBaseURI(baseUri).send();
            if (!uriReceipt.isStatusOK()) {
                throw new MintException("setBaseURI status not OK: " + uriReceipt.getTransactionHash());
            }
            System.out.println("baseURI 설정 완료: " + uriReceipt.getTransactionHash());
        } catch (Exception e) {
            throw new MintException("Failed to set baseURI: " + e.getMessage());
        }

        try {
            TransactionReceipt receipt = nftContract.mintItemForTask(userAddress, taskId, amount).send();
            if (!receipt.isStatusOK()) {
                throw new MintException("mintItemForTask status not OK: " + receipt.getTransactionHash());
            }
            System.out.println("NFT minted successfully: " + receipt.getTransactionHash());
        } catch (Exception e) {
            throw new MintException("Failed to mint NFT: " + e.getMessage());
        }

        // TODO: 단순 슬립 대신 블록 확정까지 기다리고 진행하는 로직으로 교체 필요.
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return getNftMetadata(taskId);
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
