package io.openur.domain.NFT.service;

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
