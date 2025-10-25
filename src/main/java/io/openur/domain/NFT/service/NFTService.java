package io.openur.domain.NFT.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.openur.domain.NFT.contract.NFTContract;
import io.openur.domain.NFT.dto.NFTMetadataDto;
import io.openur.global.security.UserDetailsImpl; // 실제 경로 확인 필요

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.util.Random;
import java.net.URL;

@Service
public class NFTService {

    private final Web3j web3j;
    private final Credentials credentials;
    private final NFTContract nftContract;

    @Value("${nft.base-uri}")
    private String baseUri;
    @Value("${nft.rpc-url}")
    private String rpcUrl;
    @Value("${nft.private-key}")
    private String privateKey;
    @Value("${nft.contract-address}")
    private String contractAddress;

    public NFTService() throws Exception {
        this.web3j = Web3j.build(new HttpService(rpcUrl));
        this.credentials = Credentials.create(privateKey);
        this.nftContract = NFTContract.load(contractAddress, web3j, credentials, new DefaultGasProvider());
    }

    public NFTMetadataDto mintNFT(UserDetailsImpl userDetails) throws Exception {
        String blockchainAddress = userDetails.getUser().getBlockchainAddress();

        //BigInteger tokenId = generateRandomTokenId();

        //현재는 test를 위해 77로 고정해둠
        BigInteger taskId = BigInteger.valueOf(77);
        BigInteger amount = BigInteger.ONE;
        BigInteger itemId = BigInteger.valueOf(77);

        //task 등록
        TransactionReceipt taskReceipt = nftContract.setTaskItem(taskId, itemId).send();
        if (!taskReceipt.isStatusOK()) {
            throw new RuntimeException("Failed to set task item: " + taskReceipt.getTransactionHash());
        }
        System.out.println("taskId " + taskId + "에 itemId " + itemId + " 등록 완료");

        //baseURI 설정
        TransactionReceipt uriReceipt = nftContract.setBaseURI(baseUri).send();
        if (!uriReceipt.isStatusOK()) {
            throw new RuntimeException("Failed to set baseURI: " + uriReceipt.getTransactionHash());
        }
        System.out.println("baseURI 설정 완료: " + uriReceipt.getTransactionHash());

        //NFT 민팅
        TransactionReceipt receipt = nftContract.mintItemForTask(blockchainAddress, taskId, amount).send();
        if (!receipt.isStatusOK()) {
            throw new RuntimeException("NFT minting failed: " + receipt.getTransactionHash());
        }

        //블록 포함 대기. 이거는 구글링하면서 적절한 시간 찾는 중. 지금 구조가
        //success 완료 후에 true를 전달하려고 하는데, mint success가 되더라도 실제 조회되는 시간은 다르더라.
        //물론 래리블에서 볼 경우에 문제가 되는거고, 우리 아바타 인벤토리에서 보여주는건 문제가 안되서 실제로는 안필요하긴함.
        //우리의 시나리오 상 래리블 넘어가서 보여줄게 아니면 sleep 자체가 필요없음.
        //그러나 ㅈ승룡 왈 mintNFT에 대한 return 값으로 메타데이터 내용이 필요하다. sleep은 무조건 필요
        Thread.sleep(1000);

        return getNftMetadata(taskId);
    }

    public NFTMetadataDto getNftMetadata(BigInteger tokenId) throws Exception {
        String tokenUri = nftContract.uri(tokenId).send();
        // tokenUri가 ipfs:// 형식일 경우 gateway로 변환
        String resolvedUri = tokenUri.replace("ipfs://", "https://gateway.pinata.cloud/ipfs/");

        // JSON 파싱
        URL url = new URL(resolvedUri);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(url);

        String name = rootNode.path("name").asText();
        String description = rootNode.path("description").asText();
        String image = rootNode.path("image").asText();
        String category = rootNode.path("attributes").get(0).path("value").asText();
        String rarity = rootNode.path("attributes").get(1).path("value").asText();

        return NFTMetadataDto.builder()
            .name(name)
            .description(description)
            .image(image)
            .category(category)
            .rarity(rarity)
            .build();
    }

    private BigInteger generateRandomTokenId() {
        Random random = new Random();
        return BigInteger.valueOf(Math.abs(random.nextLong())); // 이거는 추후 변경 필요. 우리 회의 때 얘기해야할 필요가 있음.
    }
}
