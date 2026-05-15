package io.openur.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.openur.config.TestSupport;
import io.openur.domain.NFT.service.NftMintClient;
import io.openur.domain.NFT.service.NftMintJobProcessor;
import java.math.BigInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class CompletedChallengeWithNftApiTest extends TestSupport {

    private static final String LIST_PATH = "/v1/challenges/completed-with-nft";
    private static final String MINT_JOB_PATH = "/v1/nft/mint-jobs";

    @MockBean
    private NftMintClient nftMintClient;

    @Autowired
    private NftMintJobProcessor nftMintJobProcessor;

    @Test
    @DisplayName("로그인 없이는 완료-NFT 도전과제 목록을 조회할 수 없다")
    void getCompletedChallengeListWithNft_withoutAuth_isForbidden() throws Exception {
        mockMvc.perform(
            get(LIST_PATH)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("nftCompleted=false 인 보상 수령 가능 도전과제는 결과에 포함되지 않는다")
    void getCompletedChallengeListWithNft_beforeMint_isEmpty() throws Exception {
        mockMvc.perform(
            get(LIST_PATH)
                .header(AUTH_HEADER, getTestUserToken1())
                .param("page", "0")
                .param("limit", "10")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("success"))
            .andExpect(jsonPath("$.data", hasSize(0)))
            .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("민팅 SUCCESS 이후 해당 도전과제와 발급된 NFT 정보가 함께 반환된다")
    void getCompletedChallengeListWithNft_afterMint_returnsChallengeWithNft() throws Exception {
        when(nftMintClient.mintToken(any(String.class), any(BigInteger.class), any(BigInteger.class)))
            .thenReturn("0xtxhash");

        Long mintJobId = createMintJobAndGetId(1L);
        nftMintJobProcessor.process(mintJobId);

        mockMvc.perform(
            get(LIST_PATH)
                .header(AUTH_HEADER, getTestUserToken1())
                .param("page", "0")
                .param("limit", "10")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("success"))
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].userChallengeId").value(1))
            .andExpect(jsonPath("$.data[0].challengeName").exists())
            .andExpect(jsonPath("$.data[0].completedDate").exists())
            .andExpect(jsonPath("$.data[0].stageCount").exists())
            .andExpect(jsonPath("$.data[0].nft.tokenId").exists())
            .andExpect(jsonPath("$.data[0].nft.transactionHash").value("0xtxhash"))
            .andExpect(jsonPath("$.data[0].nft.name").value("shoes1"))
            .andExpect(jsonPath("$.data[0].nft.image").exists())
            .andExpect(jsonPath("$.data[0].nft.category").exists())
            .andExpect(jsonPath("$.data[0].nft.rarity").exists())
            .andExpect(jsonPath("$.data[0].nft.mintedAt").exists());
    }

    @Test
    @DisplayName("두 도전과제 모두 발급되면 최근 완료가 먼저 나오고 페이지네이션이 동작한다")
    void getCompletedChallengeListWithNft_paginationAndOrder() throws Exception {
        when(nftMintClient.mintToken(any(String.class), any(BigInteger.class), any(BigInteger.class)))
            .thenReturn("0xtxhash");

        nftMintJobProcessor.process(createMintJobAndGetId(1L));
        nftMintJobProcessor.process(createMintJobAndGetId(2L));

        mockMvc.perform(
            get(LIST_PATH)
                .header(AUTH_HEADER, getTestUserToken1())
                .param("page", "0")
                .param("limit", "1")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.first").value(true))
            .andExpect(jsonPath("$.last").value(false));

        mockMvc.perform(
            get(LIST_PATH)
                .header(AUTH_HEADER, getTestUserToken1())
                .param("page", "1")
                .param("limit", "1")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    @DisplayName("타 사용자의 발급 NFT 도전과제는 누출되지 않는다")
    void getCompletedChallengeListWithNft_otherUserCannotSee() throws Exception {
        when(nftMintClient.mintToken(any(String.class), any(BigInteger.class), any(BigInteger.class)))
            .thenReturn("0xtxhash");

        nftMintJobProcessor.process(createMintJobAndGetId(1L));

        mockMvc.perform(
            get(LIST_PATH)
                .header(AUTH_HEADER, getTestUserToken2())
                .param("page", "0")
                .param("limit", "10")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(0)))
            .andExpect(jsonPath("$.totalElements").value(0));
    }

    private Long createMintJobAndGetId(Long userChallengeId) throws Exception {
        MvcResult result = mockMvc.perform(post(MINT_JOB_PATH)
                .header(AUTH_HEADER, getTestUserToken1())
                .param("userChallengeId", String.valueOf(userChallengeId))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode root = new ObjectMapper().readTree(result.getResponse().getContentAsString());
        long mintJobId = root.path("data").path("mintJobId").asLong();
        assertThat(mintJobId).isPositive();
        return mintJobId;
    }
}
