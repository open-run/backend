package io.openur.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

@Transactional(readOnly = true)
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
    @DisplayName("로그인 유저의 지갑 잔액이 있는 thumbnail token NFT Item만 조회된다")
    void getMyNftAvatarItems_ownedItemsOnly() throws Exception {
        String token = getTestUserToken1();
        Map<String, BigInteger> balances = new LinkedHashMap<>();
        balances.put("100", BigInteger.ONE);
        balances.put("200", BigInteger.TWO);
        balances.put("300", BigInteger.ZERO);
        when(nftBalanceReader.getBalances(USER_ADDRESS, List.of("100", "200", "300")))
            .thenReturn(balances);

        mockMvc.perform(
            get(PREFIX)
                .header(AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Owned NFT avatar items fetched successfully"))
            .andExpect(jsonPath("$.data", hasSize(2)))
            .andExpect(jsonPath("$.data[0].nftItemId").value(1))
            .andExpect(jsonPath("$.data[0].tokenId").value("100"))
            .andExpect(jsonPath("$.data[0].balance").value("1"))
            .andExpect(jsonPath("$.data[0].name").value("테스트 상의"))
            .andExpect(jsonPath("$.data[0].category").value("top"))
            .andExpect(jsonPath("$.data[0].rarity").value("common"))
            .andExpect(jsonPath("$.data[0].thumbnailStorageKey").value("nft-assets/v1/nft-items/top/1/thumbnail.png"))
            .andExpect(jsonPath("$.data[0].thumbnailUrl").value(nullValue()))
            .andExpect(jsonPath("$.data[0].equipImages", hasSize(1)))
            .andExpect(jsonPath("$.data[0].equipImages[0].equipPosition").value("single"))
            .andExpect(jsonPath("$.data[0].equipImages[0].storageKey").value("nft-assets/v1/nft-items/top/1/equip/single.png"))
            .andExpect(jsonPath("$.data[1].nftItemId").value(2))
            .andExpect(jsonPath("$.data[1].tokenId").value("200"))
            .andExpect(jsonPath("$.data[1].balance").value("2"))
            .andExpect(jsonPath("$.data[1].name").value("테스트 헤어"))
            .andExpect(jsonPath("$.data[1].category").value("hair"))
            .andExpect(jsonPath("$.data[1].equipImages", hasSize(2)))
            .andExpect(jsonPath("$.data[1].equipImages[0].equipPosition").value("back"))
            .andExpect(jsonPath("$.data[1].equipImages[1].equipPosition").value("front"));

        verify(nftBalanceReader).getBalances(USER_ADDRESS, List.of("100", "200", "300"));
    }
}
