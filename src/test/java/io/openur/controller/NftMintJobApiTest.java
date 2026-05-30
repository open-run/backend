package io.openur.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.openur.config.TestSupport;
import io.openur.domain.NFT.entity.NftMintJobEntity;
import io.openur.domain.NFT.enums.NftMintJobStatus;
import io.openur.domain.NFT.repository.NftMintJobJpaRepository;
import io.openur.domain.NFT.service.NftMintJobProcessor;
import io.openur.domain.userchallenge.repository.UserChallengeJpaRepository;
import java.math.BigInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class NftMintJobApiTest extends TestSupport {

    private static final String PREFIX = "/v1/nft/mint-jobs";

    @Autowired
    private NftMintJobProcessor nftMintJobProcessor;

    @Autowired
    private UserChallengeJpaRepository userChallengeJpaRepository;

    @Autowired
    private NftMintJobJpaRepository nftMintJobJpaRepository;

    @Test
    @DisplayName("보상 가능한 userChallengeId로 민팅 작업을 생성한다")
    void createMintJob_withRewardableUserChallenge_isOk() throws Exception {
        when(nftMintClient.mintToken(any(String.class), any(BigInteger.class), any(BigInteger.class)))
            .thenReturn("0xtxhash");

        mockMvc.perform(post(PREFIX)
                .header(AUTH_HEADER, getTestUserToken1())
                .param("userChallengeId", "1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("NFT mint job accepted"))
            .andExpect(jsonPath("$.data.userChallengeId").value(1))
            .andExpect(jsonPath("$.data.challengeName").value("test_challenge"))
            .andExpect(jsonPath("$.data.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("동일한 userChallengeId로 연속 요청해도 민팅 작업을 중복 생성하지 않는다")
    void createMintJob_duplicateRequest_returnsExistingJob() throws Exception {
        when(nftMintClient.mintToken(any(String.class), any(BigInteger.class), any(BigInteger.class)))
            .thenReturn("0xtxhash");

        mockMvc.perform(post(PREFIX)
                .header(AUTH_HEADER, getTestUserToken1())
                .param("userChallengeId", "1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        mockMvc.perform(post(PREFIX)
                .header(AUTH_HEADER, getTestUserToken1())
                .param("userChallengeId", "1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.userChallengeId").value(1));

        NftMintJobEntity job = nftMintJobJpaRepository
            .findByUserChallengeEntityUserChallengeId(1L)
            .orElseThrow();
        assertThat(job.getUserChallengeEntity().getUserChallengeId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("다른 사용자의 userChallengeId로 민팅 작업을 생성할 수 없다")
    void createMintJob_withOtherUsersUserChallenge_isForbidden() throws Exception {
        mockMvc.perform(post(PREFIX)
                .header(AUTH_HEADER, getTestUserToken2())
                .param("userChallengeId", "1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("민팅 작업 성공 시 job이 SUCCESS가 되고 userChallenge의 nftCompleted가 true가 된다")
    void processMintJob_success_updatesJobAndUserChallenge() throws Exception {
        when(nftMintClient.mintToken(any(String.class), any(BigInteger.class), any(BigInteger.class)))
            .thenReturn("0xtxhash");

        Long mintJobId = createMintJobAndGetId();

        nftMintJobProcessor.process(mintJobId);

        NftMintJobEntity job = nftMintJobJpaRepository.findById(mintJobId).orElseThrow();
        assertThat(job.getStatus()).isEqualTo(NftMintJobStatus.SUCCESS);
        assertThat(job.getTransactionHash()).isEqualTo("0xtxhash");
        assertThat(job.getTokenId()).isNotNull();
        assertThat(job.getNftName()).isEqualTo("shoes1");

        assertThat(userChallengeJpaRepository.findById(1L).orElseThrow().getNftCompleted()).isTrue();
    }

    @Test
    @DisplayName("민팅 작업 실패 시 job이 FAILED가 되고 userChallenge의 nftCompleted는 false로 유지된다")
    void processMintJob_failure_updatesJobOnly() throws Exception {
        when(nftMintClient.mintToken(any(String.class), any(BigInteger.class), any(BigInteger.class)))
            .thenThrow(new RuntimeException("chain down"));

        Long mintJobId = createMintJobAndGetId();

        nftMintJobProcessor.process(mintJobId);

        NftMintJobEntity job = nftMintJobJpaRepository.findById(mintJobId).orElseThrow();
        assertThat(job.getStatus()).isEqualTo(NftMintJobStatus.FAILED);
        assertThat(job.getErrorMessage()).contains("chain down");

        assertThat(userChallengeJpaRepository.findById(1L).orElseThrow().getNftCompleted()).isFalse();
    }

    private Long createMintJobAndGetId() throws Exception {
        MvcResult result = mockMvc.perform(post(PREFIX)
                .header(AUTH_HEADER, getTestUserToken1())
                .param("userChallengeId", "1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode root = new ObjectMapper().readTree(result.getResponse().getContentAsString());
        return root.path("data").path("mintJobId").asLong();
    }
}
