package io.openur.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import io.openur.config.TestSupport;
import io.openur.domain.user.dto.GetUserResponseDto;
import io.openur.global.dto.Response;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

public class UserApiTest extends TestSupport {

    private static final String PREFIX = "/v1/users";

    @Test
    @DisplayName("닉네임 중복확인")
    void getExistNicknameTest() throws Exception {
        String testName = "zXc9U1i01";

        mockMvc.perform(
                get(PREFIX + "/nickname/exist?nickname={nickname}", testName)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("유저 정보")
    void getUserInfoTest() throws Exception {
        String token = getTestUserToken1();

        mockMvc.perform(
                get(PREFIX)
                    .header(AUTH_HEADER, token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.profileImageUrl").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    @DisplayName("프로필 요약")
    void getProfileSummaryTest() throws Exception {
        when(nftMintClient.mintToken(any(String.class), any(BigInteger.class), any(BigInteger.class)))
            .thenReturn("0xtxhash");

        mockMvc.perform(
            post("/v1/nft/mint-jobs")
                .header(AUTH_HEADER, getTestUserToken1())
                .param("userChallengeId", "1")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

        mockMvc.perform(
            post("/v1/nft/mint-jobs")
                .header(AUTH_HEADER, getTestUserToken1())
                .param("userChallengeId", "2")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

        var feedbackRequest = new HashMap<>();
        feedbackRequest.put("bungId", "c0477004-1632-455f-acc9-04584b55921f");
        feedbackRequest.put("targetUserIds", List.of("9e1bfc60-f76a-47dc-9147-803653707192"));

        mockMvc.perform(
            patch(PREFIX + "/feedback")
                .header(AUTH_HEADER, getTestUserToken2())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonify(feedbackRequest))
        ).andExpect(status().isOk());

        mockMvc.perform(
            get(PREFIX + "/profile-summary")
                .header(AUTH_HEADER, getTestUserToken1())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("success"))
            .andExpect(jsonPath("$.data.receivedLikeCount").value(1))
            .andExpect(jsonPath("$.data.currentOwnedBungCount").value(1))
            .andExpect(jsonPath("$.data.acquiredNftCount").value(2))
            .andExpect(jsonPath("$.data.recentAcquiredNfts", hasSize(2)))
            .andExpect(jsonPath("$.data.recentAcquiredNfts[0].challengeName").exists())
            .andExpect(jsonPath("$.data.recentAcquiredNfts[0].acquiredAt").exists())
            .andExpect(jsonPath("$.data.recentAcquiredNfts[0].nft.transactionHash").value("0xtxhash"));
    }

    @Nested
    @DisplayName("유저 닉네임으로 검색")
    class getUserByNicknameTest {

        @Test
        @DisplayName("200 Ok.")
        void getUserByNickname_isOk() throws Exception {
            String token = getTestUserToken1();

            MvcResult result = mockMvc.perform(
                    get(PREFIX + "/nickname?nickname={nickname}", "test")
                        .header(AUTH_HEADER, token)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk()).andReturn();

            Response<List<GetUserResponseDto>> response = parseResponse(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
            );
            assert response.getData().size() == 3;
        }

        @Test
        @DisplayName("400 Bad Request. Query parameter is required.")
        void getUserByNickname_isBadRequest() throws Exception {
            String token = getTestUserToken1();

            mockMvc.perform(
                    get(PREFIX + "/nickname")
                        .header(AUTH_HEADER, token)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("페이스 기록 입력 저장")
    class saveSurveyResultTest {

        @Test
        @DisplayName("400 Bad Request. 잘못된 형식의 인풋")
        void saveSurveyResult_isBadRequest() throws Exception {
            String token = getTestUserToken1();
            var surveyResult = new HashMap<>();
            surveyResult.put("nickname", "나도모른거");
            surveyResult.put("runningPace", "5'55\"");  // 형식에 맞지 않음. 5'55" -> 05'55"
            surveyResult.put("runningFrequency", "2");
            mockMvc.perform(
                    patch(PREFIX)
                        .header(AUTH_HEADER, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonify(surveyResult))
                )
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("201 Created.")
        @Transactional
        void saveSurveyResult_isOk() throws Exception {
            String token = getTestUserToken1();
            var surveyResult = new HashMap<>();
            surveyResult.put("nickname", "나도모른거");
            surveyResult.put("runningPace", "05'55\"");
            surveyResult.put("runningFrequency", 2);
            mockMvc.perform(
                    patch(PREFIX)
                        .header(AUTH_HEADER, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonify(surveyResult))
                )
                .andExpect(status().isCreated());
        }
    }
}
