package io.openur.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.openur.config.TestSupport;
import io.openur.domain.NFT.service.NftBalanceReader;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class NftAvatarItemApiTest extends TestSupport {

    private static final String PREFIX = "/v1/nft/avatar-items/me";
    private static final String USER_ADDRESS = "0x1234567890123456789012345678901234567890";

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
            put(PREFIX + "/wearing")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
        )
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("로컬 프론트엔드의 착용 저장 preflight 요청을 허용한다")
    void wearingAvatarSave_preflightFromLocalFrontend_isAllowed() throws Exception {
        mockMvc.perform(
            options(PREFIX + "/wearing")
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
    @DisplayName("착용 NFT 아바타를 저장하고 다시 조회한다")
    void saveAndGetWearingAvatar() throws Exception {
        String token = getTestUserToken1();
        when(nftBalanceReader.getBalances(USER_ADDRESS, List.of("100", "200", "500")))
            .thenReturn(Map.of(
                "100", BigInteger.ONE,
                "200", BigInteger.ONE,
                "500", BigInteger.ONE
            ));

        mockMvc.perform(
            put(PREFIX + "/wearing")
                .header(AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
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
                    """)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Wearing NFT avatar saved successfully"))
            .andExpect(jsonPath("$.data.fullSet").value(nullValue()))
            .andExpect(jsonPath("$.data.upperClothing.nftItemId").value(1))
            .andExpect(jsonPath("$.data.upperClothing.mainCategory").value("upperClothing"))
            .andExpect(jsonPath("$.data.hair.nftItemId").value(2))
            .andExpect(jsonPath("$.data.hair.imageUrl", hasSize(2)))
            .andExpect(jsonPath("$.data.accessories.eye-accessories.nftItemId").value(6))
            .andExpect(jsonPath("$.data.accessories.eye-accessories.subCategory").value("eye-accessories"));

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
    @DisplayName("착용 슬롯과 NFT Item 카테고리가 맞지 않으면 저장할 수 없다")
    void saveWearingAvatar_rejectsSlotMismatch() throws Exception {
        String token = getTestUserToken1();

        mockMvc.perform(
            put(PREFIX + "/wearing")
                .header(AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "upperClothing": 2
                    }
                    """)
        )
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("보유하지 않은 NFT Item은 착용 저장할 수 없다")
    void saveWearingAvatar_rejectsUnownedItem() throws Exception {
        String token = getTestUserToken1();
        when(nftBalanceReader.getBalances(USER_ADDRESS, List.of("100")))
            .thenReturn(Map.of("100", BigInteger.ZERO));

        mockMvc.perform(
            put(PREFIX + "/wearing")
                .header(AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "upperClothing": 1
                    }
                    """)
        )
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("fullSet은 현재 지원하지 않는다")
    void saveWearingAvatar_rejectsFullSet() throws Exception {
        String token = getTestUserToken1();

        mockMvc.perform(
            put(PREFIX + "/wearing")
                .header(AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "fullSet": 1
                    }
                    """)
        )
            .andExpect(status().isBadRequest());
    }
}
