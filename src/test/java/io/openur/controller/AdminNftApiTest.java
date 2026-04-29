package io.openur.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.openur.config.TestSupport;
import io.openur.domain.NFT.service.NftMintClient;
import io.openur.domain.user.entity.UserEntity;
import io.openur.domain.user.model.Provider;
import io.openur.domain.user.repository.UserJpaRepository;
import java.math.BigInteger;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class AdminNftApiTest extends TestSupport {

    private static final String PREFIX = "/v1/admin";
    private static final String RECIPIENT_ADDRESS = "0x9999999999999999999999999999999999999999";

    @MockBean
    private NftMintClient nftMintClient;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Test
    @DisplayName("로그인하지 않으면 admin API를 사용할 수 없다")
    void adminApi_withoutAuthentication_isForbidden() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get(PREFIX + "/users"))
            .andExpect(status().is4xxClientError());

        mockMvc.perform(get(PREFIX + "/nft/avatar-items"))
            .andExpect(status().is4xxClientError());

        mockMvc.perform(get(PREFIX + "/nft/avatar-items/try-on"))
            .andExpect(status().is4xxClientError());

        mockMvc.perform(post(PREFIX + "/nft/avatar-items/grants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("admin 여부를 로그인 유저 address 기준으로 반환한다")
    void getAdminMe_returnsAdminStatus() throws Exception {
        mockMvc.perform(get(PREFIX + "/me")
                .header(AUTH_HEADER, getTestUserToken1()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.admin").value(true));

        mockMvc.perform(get(PREFIX + "/me")
                .header(AUTH_HEADER, getTestUserToken2()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.admin").value(false));
    }

    @Test
    @DisplayName("allowlist에 없는 유저는 admin NFT 목록과 부여 API를 사용할 수 없다")
    void adminNftApi_nonAdmin_isForbidden() throws Exception {
        String nonAdminToken = getTestUserToken2();

        mockMvc.perform(get(PREFIX + "/users")
                .header(AUTH_HEADER, nonAdminToken))
            .andExpect(status().isForbidden());

        mockMvc.perform(get(PREFIX + "/nft/avatar-items")
                .header(AUTH_HEADER, nonAdminToken))
            .andExpect(status().isForbidden());

        mockMvc.perform(get(PREFIX + "/nft/avatar-items/try-on")
                .header(AUTH_HEADER, nonAdminToken))
            .andExpect(status().isForbidden());

        mockMvc.perform(post(PREFIX + "/nft/avatar-items/grants")
                .header(AUTH_HEADER, nonAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "recipientAddress": "0x9999999999999999999999999999999999999999",
                      "nftItemId": 1
                    }
                    """))
            .andExpect(status().isForbidden());

        verifyNoInteractions(nftMintClient);
    }

    @Test
    @DisplayName("로컬 프론트엔드의 admin users preflight와 실제 조회 응답에 CORS 헤더를 포함한다")
    void adminUsers_corsFromLocalFrontend_isAllowed() throws Exception {
        mockMvc.perform(
            options(PREFIX + "/users")
                .header("Origin", "http://localhost:6050")
                .header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", "authorization,content-type")
        )
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:6050"))
            .andExpect(header().string("Access-Control-Allow-Methods", org.hamcrest.Matchers.containsString("GET")))
            .andExpect(header().string("Access-Control-Allow-Headers", org.hamcrest.Matchers.containsString("authorization")));

        mockMvc.perform(get(PREFIX + "/users")
                .header("Origin", "http://localhost:6050")
                .header(AUTH_HEADER, getTestUserToken1()))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:6050"));
    }

    @Test
    @DisplayName("admin은 유효한 wallet address를 가진 비차단 유저 목록을 nickname 기준으로 조회한다")
    void getAdminUsers_returnsGrantableUsers() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        userJpaRepository.save(new UserEntity(
            "10000000-0000-0000-0000-000000000001",
            "zzz",
            false,
            Provider.smart_wallet,
            false,
            now,
            now,
            "0x2222222222222222222222222222222222222222",
            null,
            null,
            0
        ));
        userJpaRepository.save(new UserEntity(
            "10000000-0000-0000-0000-000000000002",
            null,
            false,
            Provider.smart_wallet,
            false,
            now,
            now,
            "0x3333333333333333333333333333333333333333",
            null,
            null,
            0
        ));
        userJpaRepository.save(new UserEntity(
            "10000000-0000-0000-0000-000000000003",
            "aaa-invalid",
            false,
            Provider.smart_wallet,
            false,
            now,
            now,
            "not-address",
            null,
            null,
            0
        ));
        userJpaRepository.save(new UserEntity(
            "10000000-0000-0000-0000-000000000004",
            "bbb-blocked",
            false,
            Provider.smart_wallet,
            true,
            now,
            now,
            "0x4444444444444444444444444444444444444444",
            null,
            null,
            0
        ));

        mockMvc.perform(get(PREFIX + "/users")
                .header(AUTH_HEADER, getTestUserToken1()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Admin users fetched successfully"))
            .andExpect(jsonPath("$.data", hasSize(5)))
            .andExpect(jsonPath("$.data[0].nickname").value("test"))
            .andExpect(jsonPath("$.data[0].blockchainAddress").value("0x1234567890123456789012345678901234567890"))
            .andExpect(jsonPath("$.data[3].nickname").value("zzz"))
            .andExpect(jsonPath("$.data[4].nickname").value(nullValue()))
            .andExpect(jsonPath("$.data[*].blockchainAddress").value(not(hasItem("not-address"))))
            .andExpect(jsonPath("$.data[*].blockchainAddress").value(not(hasItem("0x4444444444444444444444444444444444444444"))));
    }

    @Test
    @DisplayName("admin은 민팅 완료된 enabled NFT Item 목록만 조회한다")
    void getMintedNftAvatarItems_adminOnly() throws Exception {
        mockMvc.perform(get(PREFIX + "/nft/avatar-items")
                .header(AUTH_HEADER, getTestUserToken1()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Admin NFT avatar items fetched successfully"))
            .andExpect(jsonPath("$.data", hasSize(5)))
            .andExpect(jsonPath("$.data[0].nftItemId").value(1))
            .andExpect(jsonPath("$.data[0].tokenId").value("100"))
            .andExpect(jsonPath("$.data[0].name").value("테스트 상의"))
            .andExpect(jsonPath("$.data[0].category").value("top"))
            .andExpect(jsonPath("$.data[0].mainCategory").value("upperClothing"))
            .andExpect(jsonPath("$.data[0].subCategory").value(nullValue()))
            .andExpect(jsonPath("$.data[0].rarity").value("common"))
            .andExpect(jsonPath("$.data[0].thumbnailStorageKey").value("nft-assets/v1/nft-items/top/1/thumbnail.png"))
            .andExpect(jsonPath("$.data[0].thumbnailUrl").value("http://localhost:8080/local-nft-assets/nft-assets/v1/nft-items/top/1/thumbnail.png"))
            .andExpect(jsonPath("$.data[3].nftItemId").value(6))
            .andExpect(jsonPath("$.data[3].mainCategory").value("accessories"))
            .andExpect(jsonPath("$.data[3].subCategory").value("eye-accessories"))
            .andExpect(jsonPath("$.data[*].nftItemId").value(not(hasItem(3))))
            .andExpect(jsonPath("$.data[*].nftItemId").value(not(hasItem(5))));
    }

    @Test
    @DisplayName("admin은 enabled NFT Item 전체를 아바타 장착 테스트용으로 조회한다")
    void getTryOnNftAvatarItems_returnsEnabledItems() throws Exception {
        mockMvc.perform(get(PREFIX + "/nft/avatar-items/try-on")
                .header(AUTH_HEADER, getTestUserToken1()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Admin NFT avatar try-on items fetched successfully"))
            .andExpect(jsonPath("$.data", hasSize(8)))
            .andExpect(jsonPath("$.data[0].id").value("1"))
            .andExpect(jsonPath("$.data[0].nftItemId").value(1))
            .andExpect(jsonPath("$.data[0].tokenId").value("100"))
            .andExpect(jsonPath("$.data[0].name").value("테스트 상의"))
            .andExpect(jsonPath("$.data[0].mainCategory").value("upperClothing"))
            .andExpect(jsonPath("$.data[0].subCategory").value(nullValue()))
            .andExpect(jsonPath("$.data[0].rarity").value("common"))
            .andExpect(jsonPath("$.data[0].imageUrl").value("http://localhost:8080/local-nft-assets/nft-assets/v1/nft-items/top/1/equip/single.png"))
            .andExpect(jsonPath("$.data[0].storageKey").value("nft-assets/v1/nft-items/top/1/equip/single.png"))
            .andExpect(jsonPath("$.data[0].thumbnailStorageKey").value("nft-assets/v1/nft-items/top/1/thumbnail.png"))
            .andExpect(jsonPath("$.data[0].thumbnailUrl").value("http://localhost:8080/local-nft-assets/nft-assets/v1/nft-items/top/1/thumbnail.png"))
            .andExpect(jsonPath("$.data[1].nftItemId").value(2))
            .andExpect(jsonPath("$.data[1].imageUrl", hasSize(2)))
            .andExpect(jsonPath("$.data[1].imageUrl[0]").value("http://localhost:8080/local-nft-assets/nft-assets/v1/nft-items/hair/2/equip/front.png"))
            .andExpect(jsonPath("$.data[1].imageUrl[1]").value("http://localhost:8080/local-nft-assets/nft-assets/v1/nft-items/hair/2/equip/back.png"))
            .andExpect(jsonPath("$.data[*].nftItemId").value(hasItem(3)))
            .andExpect(jsonPath("$.data[*].nftItemId").value(not(hasItem(5))))
            .andExpect(jsonPath("$.data[6].nftItemId").value(8))
            .andExpect(jsonPath("$.data[6].name").value("장착 이미지 없는 아이템"))
            .andExpect(jsonPath("$.data[6].imageUrl").value(nullValue()))
            .andExpect(jsonPath("$.data[6].storageKey").value(nullValue()))
            .andExpect(jsonPath("$.data[6].thumbnailUrl").value("http://localhost:8080/local-nft-assets/nft-assets/v1/nft-items/body_acc/8/thumbnail.png"))
            .andExpect(jsonPath("$.data[7].nftItemId").value(9))
            .andExpect(jsonPath("$.data[7].name").value("장착 이미지 없는 헤어"))
            .andExpect(jsonPath("$.data[7].imageUrl").value(nullValue()))
            .andExpect(jsonPath("$.data[7].storageKey").value(nullValue()))
            .andExpect(jsonPath("$.data[7].thumbnailUrl").value("http://localhost:8080/local-nft-assets/nft-assets/v1/nft-items/hair/9/thumbnail.png"));
    }

    @Test
    @DisplayName("admin은 mint 완료된 NFT Item을 대상 address에 추가 mint로 부여한다")
    void grantNftAvatarItem_mintsTokenToRecipient() throws Exception {
        when(nftMintClient.mintToken(
            RECIPIENT_ADDRESS,
            BigInteger.valueOf(100),
            BigInteger.ONE
        )).thenReturn("0xtxhash");

        mockMvc.perform(post(PREFIX + "/nft/avatar-items/grants")
                .header(AUTH_HEADER, getTestUserToken1())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "recipientAddress": "0x9999999999999999999999999999999999999999",
                      "nftItemId": 1
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Admin NFT avatar item granted successfully"))
            .andExpect(jsonPath("$.data.recipientAddress").value(RECIPIENT_ADDRESS))
            .andExpect(jsonPath("$.data.nftItemId").value(1))
            .andExpect(jsonPath("$.data.tokenId").value("100"))
            .andExpect(jsonPath("$.data.transactionHash").value("0xtxhash"));

        verify(nftMintClient).mintToken(RECIPIENT_ADDRESS, BigInteger.valueOf(100), BigInteger.ONE);
    }

    @Test
    @DisplayName("mint token이 없는 NFT Item은 부여할 수 없다")
    void grantNftAvatarItem_rejectsUnmintedItem() throws Exception {
        mockMvc.perform(post(PREFIX + "/nft/avatar-items/grants")
                .header(AUTH_HEADER, getTestUserToken1())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "recipientAddress": "0x9999999999999999999999999999999999999999",
                      "nftItemId": 3
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(containsString("has no minted token")));

        verifyNoInteractions(nftMintClient);
    }

    @Test
    @DisplayName("비활성 NFT Item은 부여할 수 없다")
    void grantNftAvatarItem_rejectsDisabledItem() throws Exception {
        mockMvc.perform(post(PREFIX + "/nft/avatar-items/grants")
                .header(AUTH_HEADER, getTestUserToken1())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "recipientAddress": "0x9999999999999999999999999999999999999999",
                      "nftItemId": 5
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(containsString("disabled nftItemId")));

        verifyNoInteractions(nftMintClient);
    }

    @Test
    @DisplayName("잘못된 recipient address는 부여할 수 없다")
    void grantNftAvatarItem_rejectsInvalidRecipientAddress() throws Exception {
        mockMvc.perform(post(PREFIX + "/nft/avatar-items/grants")
                .header(AUTH_HEADER, getTestUserToken1())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "recipientAddress": "not-address",
                      "nftItemId": 1
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(containsString("Invalid Ethereum address")));

        verifyNoInteractions(nftMintClient);
    }
}
