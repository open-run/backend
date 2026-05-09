package io.openur.domain.NFT.service;

// NOTE: 본 클래스는 NftRewardSelector 도입과 함께 무력화되었습니다.
// - 보상 토큰 ID는 Challenge.rewardType + ChallengeStage 가중치 기반의
//   NftRewardSelector.selectTokenId(...) 가 결정합니다.
// - 메타데이터는 tb_nft_items 카탈로그에서 직접 조립하는
//   NftRewardSelector.resolveMetadata(...) 가 제공합니다.
// classpath:metadata/{tokenId}.json + Pinata IPFS 게이트웨이 의존이 더 이상 필요 없으나,
// 향후 IPFS 메타 재도입 가능성에 대비해 코드 자체는 주석 형태로 보존합니다.
// Spring 빈으로 등록되지 않도록 @Service 와 클래스 정의 전체를 블록 주석 처리합니다.

/*
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.openur.domain.NFT.dto.NFTMetadataDto;
import io.openur.domain.NFT.exception.MintException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Hash;

@Service
public class NftRewardMetadataResolver {

    private static final String DEFAULT_REWARD_TOKEN_SEED = "shoes1";

    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    @Value("${nft.pinata-dedicated-gateway:https://gateway.pinata.cloud}")
    private String pinataDedicatedGateway;

    public NftRewardMetadataResolver(ResourceLoader resourceLoader, ObjectMapper objectMapper) {
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
    }

    public BigInteger getRewardTokenId() {
        byte[] encoded = DEFAULT_REWARD_TOKEN_SEED.getBytes(StandardCharsets.UTF_8);
        byte[] hash = Hash.sha3(encoded);
        return new BigInteger(1, hash);
    }

    public NFTMetadataDto getMetadata(BigInteger tokenId) {
        Resource resource = resourceLoader.getResource("classpath:metadata/" + tokenId + ".json");

        try {
            JsonNode rootNode = objectMapper.readTree(resource.getInputStream());
            String image = rootNode.path("image").asText();

            return NFTMetadataDto.builder()
                .tokenId(tokenId)
                .name(rootNode.path("name").asText())
                .description(rootNode.path("description").asText())
                .image(image.replace("ipfs://", pinataDedicatedGateway + "/ipfs/"))
                .category(rootNode.path("attributes").get(0).path("value").asText())
                .rarity(rootNode.path("attributes").get(1).path("value").asText())
                .build();
        } catch (IOException e) {
            throw new MintException("Failed to resolve NFT metadata: " + e.getMessage());
        }
    }
}
*/
