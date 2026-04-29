package io.openur.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.openur.config.TestSupport;
import io.openur.domain.NFT.service.NftBalanceReader;
import io.openur.domain.user.repository.UserJpaRepository;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class NftAvatarItemApiTest extends TestSupport {

    private static final String PREFIX = "/v1/nft/avatar-items/me";
    private static final String TEST_USER_ID = "9e1bfc60-f76a-47dc-9147-803653707192";
    private static final String USER_ADDRESS = "0x1234567890123456789012345678901234567890";
    private static final byte[] PNG_BYTES = new byte[] {
        (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
        0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
        0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
        0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4,
        (byte) 0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41,
        0x54, 0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00,
        0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte) 0xB4, 0x00,
        0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE,
        0x42, 0x60, (byte) 0x82
    };

    @Autowired
    private UserJpaRepository userJpaRepository;

    @MockBean
    private NftBalanceReader nftBalanceReader;

    @Test
    @DisplayName("보유 NFT 아바타 목록은 로그인 없이 조회할 수 없다")
    void getMyNftAvatarItems_withoutAuthentication_isForbidden() throws Exception {
        mockMvc.perform(
            get(PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("착용 NFT 아바타는 로그인 없이 조회하거나 저장할 수 없다")
    void wearingAvatar_withoutAuthentication_isForbidden() throws Exception {
        mockMvc.perform(
            get(PREFIX + "/wearing")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().is4xxClientError());

        mockMvc.perform(
            multipart(PREFIX + "/wearing/profile-image")
                .file(wearingAvatarPart("{}"))
                .file(profileImagePart())
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                })
        )
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("로컬 프론트엔드의 착용 저장 preflight 요청을 허용한다")
    void wearingAvatarSave_preflightFromLocalFrontend_isAllowed() throws Exception {
        mockMvc.perform(
            options(PREFIX + "/wearing/profile-image")
                .header("Origin", "http://localhost:6050")
                .header("Access-Control-Request-Method", "PUT")
                .header("Access-Control-Request-Headers", "authorization,content-type")
        )
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:6050"))
            .andExpect(header().string("Access-Control-Allow-Methods", org.hamcrest.Matchers.containsString("PUT")))
            .andExpect(header().string("Access-Control-Allow-Headers", org.hamcrest.Matchers.containsString("authorization")));
    }

    @Test
    @DisplayName("로그인 유저의 지갑 잔액이 있는 thumbnail token NFT Item만 조회된다")
    void getMyNftAvatarItems_ownedItemsOnly() throws Exception {
        String token = getTestUserToken1();
        Map<String, BigInteger> balances = new LinkedHashMap<>();
        balances.put("100", BigInteger.ONE);
        balances.put("200", BigInteger.TWO);
        balances.put("300", BigInteger.ZERO);
        balances.put("500", BigInteger.ONE);
        balances.put("600", BigInteger.ONE);
        when(nftBalanceReader.getBalances(USER_ADDRESS, List.of("100", "200", "300", "500", "600")))
            .thenReturn(balances);

        mockMvc.perform(
            get(PREFIX)
                .header(AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Owned NFT avatar items fetched successfully"))
            .andExpect(jsonPath("$.data", hasSize(4)))
            .andExpect(jsonPath("$.data[0].id").value("1"))
            .andExpect(jsonPath("$.data[0].nftItemId").value(1))
            .andExpect(jsonPath("$.data[0].tokenId").value("100"))
            .andExpect(jsonPath("$.data[0].balance").value("1"))
            .andExpect(jsonPath("$.data[0].name").value("테스트 상의"))
            .andExpect(jsonPath("$.data[0].rarity").value("common"))
            .andExpect(jsonPath("$.data[0].mainCategory").value("upperClothing"))
            .andExpect(jsonPath("$.data[0].subCategory").value(nullValue()))
            .andExpect(jsonPath("$.data[0].imageUrl").value("http://localhost:8080/local-nft-assets/nft-assets/v1/nft-items/top/1/equip/single.png"))
            .andExpect(jsonPath("$.data[0].storageKey").value("nft-assets/v1/nft-items/top/1/equip/single.png"))
            .andExpect(jsonPath("$.data[0].thumbnailStorageKey").value("nft-assets/v1/nft-items/top/1/thumbnail.png"))
            .andExpect(jsonPath("$.data[0].thumbnailUrl").value("http://localhost:8080/local-nft-assets/nft-assets/v1/nft-items/top/1/thumbnail.png"))
            .andExpect(jsonPath("$.data[1].nftItemId").value(2))
            .andExpect(jsonPath("$.data[1].tokenId").value("200"))
            .andExpect(jsonPath("$.data[1].balance").value("2"))
            .andExpect(jsonPath("$.data[1].name").value("테스트 헤어"))
            .andExpect(jsonPath("$.data[1].mainCategory").value("hair"))
            .andExpect(jsonPath("$.data[1].imageUrl", hasSize(2)))
            .andExpect(jsonPath("$.data[1].imageUrl[0]").value("http://localhost:8080/local-nft-assets/nft-assets/v1/nft-items/hair/2/equip/front.png"))
            .andExpect(jsonPath("$.data[1].imageUrl[1]").value("http://localhost:8080/local-nft-assets/nft-assets/v1/nft-items/hair/2/equip/back.png"))
            .andExpect(jsonPath("$.data[1].storageKey").value("nft-assets/v1/nft-items/hair/2/equip/front.png"))
            .andExpect(jsonPath("$.data[2].nftItemId").value(6))
            .andExpect(jsonPath("$.data[2].mainCategory").value("accessories"))
            .andExpect(jsonPath("$.data[2].subCategory").value("eye-accessories"))
            .andExpect(jsonPath("$.data[3].nftItemId").value(7))
            .andExpect(jsonPath("$.data[3].mainCategory").value("hair"))
            .andExpect(jsonPath("$.data[3].imageUrl", hasSize(1)));

        verify(nftBalanceReader).getBalances(USER_ADDRESS, List.of("100", "200", "300", "500", "600"));
    }

    @Test
    @DisplayName("착용 NFT 아바타와 프로필 이미지를 한 번에 저장한다")
    void saveWearingAvatarWithProfileImage_isOk() throws Exception {
        String token = getTestUserToken1();
        Files.deleteIfExists(profileImagePath(TEST_USER_ID));
        when(nftBalanceReader.getBalances(USER_ADDRESS, List.of("100", "200", "500")))
            .thenReturn(Map.of(
                "100", BigInteger.ONE,
                "200", BigInteger.ONE,
                "500", BigInteger.ONE
            ));

        mockMvc.perform(
            multipart(PREFIX + "/wearing/profile-image")
                .file(wearingAvatarPart("""
                    {
                      "fullSet": null,
                      "upperClothing": 1,
                      "lowerClothing": null,
                      "footwear": null,
                      "face": null,
                      "skin": null,
                      "hair": 2,
                      "accessories": {
                        "head-accessories": null,
                        "eye-accessories": 6,
                        "ear-accessories": null,
                        "body-accessories": null
                      }
                    }
                    """))
                .file(profileImagePart())
                .header(AUTH_HEADER, token)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                })
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Wearing NFT avatar and profile image saved successfully"))
            .andExpect(jsonPath("$.data.fullSet").value(nullValue()))
            .andExpect(jsonPath("$.data.upperClothing.nftItemId").value(1))
            .andExpect(jsonPath("$.data.hair.nftItemId").value(2))
            .andExpect(jsonPath("$.data.accessories.eye-accessories.nftItemId").value(6));

        assertThat(Files.exists(profileImagePath(TEST_USER_ID))).isTrue();
        assertThat(userJpaRepository.findByUserId(TEST_USER_ID).orElseThrow().getProfileImageStorageKey())
            .isEqualTo("profile-images/users/" + TEST_USER_ID + "/profile.png");

        mockMvc.perform(
            get(PREFIX + "/wearing")
                .header(AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Wearing NFT avatar fetched successfully"))
            .andExpect(jsonPath("$.data.upperClothing.nftItemId").value(1))
            .andExpect(jsonPath("$.data.hair.nftItemId").value(2))
            .andExpect(jsonPath("$.data.accessories.eye-accessories.nftItemId").value(6));
    }

    @Test
    @DisplayName("착용 검증에 실패하면 프로필 이미지를 저장하지 않는다")
    void saveWearingAvatarWithProfileImage_rejectsInvalidWearingBeforeImageStorage() throws Exception {
        String token = getTestUserToken1();
        Files.deleteIfExists(profileImagePath(TEST_USER_ID));

        mockMvc.perform(
            multipart(PREFIX + "/wearing/profile-image")
                .file(wearingAvatarPart("""
                    {
                      "upperClothing": 2
                    }
                    """))
                .file(profileImagePart())
                .header(AUTH_HEADER, token)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                })
        )
            .andExpect(status().isBadRequest());

        assertThat(Files.exists(profileImagePath(TEST_USER_ID))).isFalse();
    }

    @Test
    @DisplayName("보유하지 않은 NFT Item은 착용 저장할 수 없다")
    void saveWearingAvatarWithProfileImage_rejectsUnownedItem() throws Exception {
        String token = getTestUserToken1();
        Files.deleteIfExists(profileImagePath(TEST_USER_ID));
        when(nftBalanceReader.getBalances(USER_ADDRESS, List.of("100")))
            .thenReturn(Map.of("100", BigInteger.ZERO));

        mockMvc.perform(
            multipart(PREFIX + "/wearing/profile-image")
                .file(wearingAvatarPart("""
                    {
                      "upperClothing": 1
                    }
                    """))
                .file(profileImagePart())
                .header(AUTH_HEADER, token)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                })
        )
            .andExpect(status().isBadRequest());

        assertThat(Files.exists(profileImagePath(TEST_USER_ID))).isFalse();
    }

    @Test
    @DisplayName("fullSet은 현재 지원하지 않는다")
    void saveWearingAvatarWithProfileImage_rejectsFullSet() throws Exception {
        String token = getTestUserToken1();
        Files.deleteIfExists(profileImagePath(TEST_USER_ID));

        mockMvc.perform(
            multipart(PREFIX + "/wearing/profile-image")
                .file(wearingAvatarPart("""
                    {
                      "fullSet": 1
                    }
                    """))
                .file(profileImagePart())
                .header(AUTH_HEADER, token)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                })
        )
            .andExpect(status().isBadRequest());

        assertThat(Files.exists(profileImagePath(TEST_USER_ID))).isFalse();
    }

    private MockMultipartFile wearingAvatarPart(String json) {
        return new MockMultipartFile(
            "wearingAvatar",
            "wearingAvatar.json",
            MediaType.APPLICATION_JSON_VALUE,
            json.getBytes(StandardCharsets.UTF_8)
        );
    }

    private MockMultipartFile profileImagePart() {
        return new MockMultipartFile("image", "profile.png", MediaType.IMAGE_PNG_VALUE, PNG_BYTES);
    }

    private Path profileImagePath(String userId) {
        return Path.of("build/test-local-assets/profile-images/users/" + userId + "/profile.png");
    }
}
