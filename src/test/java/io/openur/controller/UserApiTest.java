package io.openur.controller;

import static org.assertj.core.api.Assertions.assertThat;
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
import jakarta.persistence.EntityManager;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

public class UserApiTest extends TestSupport {

    private static final String PREFIX = "/v1/users";
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private EntityManager entityManager;

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
    @Transactional
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

        String completedBungId = "a1234567-89ab-cdef-0123-456789abcdef";
        mockMvc.perform(
            patch("/v1/bungs/" + completedBungId + "/complete")
                .header(AUTH_HEADER, getTestUserToken2())
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

        var feedbackRequest = new HashMap<>();
        feedbackRequest.put("bungId", completedBungId);
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

    @Test
    @DisplayName("빈 피드백 저장도 벙 피드백 제출 완료로 기록한다")
    @Transactional
    void increaseFeedback_emptyTargetsMarksFeedbackSubmitted() throws Exception {
        String completedBungId = "a1234567-89ab-cdef-0123-456789abcdef";
        String reviewerUserId = "9e1bfc60-f76a-47dc-9147-803653707192";

        mockMvc.perform(
            patch("/v1/bungs/" + completedBungId + "/complete")
                .header(AUTH_HEADER, getTestUserToken2())
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

        var feedbackRequest = new HashMap<>();
        feedbackRequest.put("bungId", completedBungId);
        feedbackRequest.put("targetUserIds", List.of());

        mockMvc.perform(
            patch(PREFIX + "/feedback")
                .header(AUTH_HEADER, getTestUserToken1())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonify(feedbackRequest))
        ).andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        Boolean submitted = jdbcTemplate.queryForObject(
            """
                SELECT feedback_submitted_at IS NOT NULL
                FROM tb_users_bungs
                WHERE bung_id = ? AND user_id = ?
                """,
            Boolean.class,
            completedBungId,
            reviewerUserId
        );

        assertThat(submitted).isTrue();
    }

    @Test
    @DisplayName("같은 벙 멤버가 아닌 유저에게는 피드백을 남길 수 없다")
    @Transactional
    void increaseFeedback_nonMemberTargetDoesNotIncrementOrMarkSubmitted() throws Exception {
        String completedBungId = "a1234567-89ab-cdef-0123-456789abcdef";
        String reviewerUserId = "9e1bfc60-f76a-47dc-9147-803653707192";
        String nonMemberUserId = "5d22bd65-f1ed-4e7b-bc7b-0a59580d3176";

        mockMvc.perform(
            patch("/v1/bungs/" + completedBungId + "/complete")
                .header(AUTH_HEADER, getTestUserToken2())
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

        var feedbackRequest = new HashMap<>();
        feedbackRequest.put("bungId", completedBungId);
        feedbackRequest.put("targetUserIds", List.of(nonMemberUserId));

        mockMvc.perform(
            patch(PREFIX + "/feedback")
                .header(AUTH_HEADER, getTestUserToken1())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonify(feedbackRequest))
        ).andExpect(status().isNotFound());

        entityManager.flush();
        entityManager.clear();

        Integer feedback = jdbcTemplate.queryForObject(
            "SELECT feedback FROM tb_users WHERE user_id = ?",
            Integer.class,
            nonMemberUserId
        );
        Boolean submitted = jdbcTemplate.queryForObject(
            """
                SELECT feedback_submitted_at IS NOT NULL
                FROM tb_users_bungs
                WHERE bung_id = ? AND user_id = ?
                """,
            Boolean.class,
            completedBungId,
            reviewerUserId
        );

        assertThat(feedback).isZero();
        assertThat(submitted).isFalse();
    }

    @Test
    @DisplayName("자기 자신에게는 피드백을 남길 수 없다")
    @Transactional
    void increaseFeedback_selfTargetDoesNotIncrementOrMarkSubmitted() throws Exception {
        String completedBungId = "a1234567-89ab-cdef-0123-456789abcdef";
        String reviewerUserId = "9e1bfc60-f76a-47dc-9147-803653707192";

        mockMvc.perform(
            patch("/v1/bungs/" + completedBungId + "/complete")
                .header(AUTH_HEADER, getTestUserToken2())
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

        var feedbackRequest = new HashMap<>();
        feedbackRequest.put("bungId", completedBungId);
        feedbackRequest.put("targetUserIds", List.of(reviewerUserId));

        mockMvc.perform(
            patch(PREFIX + "/feedback")
                .header(AUTH_HEADER, getTestUserToken1())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonify(feedbackRequest))
        ).andExpect(status().isNotFound());

        entityManager.flush();
        entityManager.clear();

        Integer feedback = jdbcTemplate.queryForObject(
            "SELECT feedback FROM tb_users WHERE user_id = ?",
            Integer.class,
            reviewerUserId
        );
        Boolean submitted = jdbcTemplate.queryForObject(
            """
                SELECT feedback_submitted_at IS NOT NULL
                FROM tb_users_bungs
                WHERE bung_id = ? AND user_id = ?
                """,
            Boolean.class,
            completedBungId,
            reviewerUserId
        );

        assertThat(feedback).isZero();
        assertThat(submitted).isFalse();
    }

    @Test
    @DisplayName("일부 target 이 유효하지 않으면 유효한 유저도 부분 증가시키지 않는다")
    @Transactional
    void increaseFeedback_mixedInvalidTargetDoesNotPartiallyIncrement() throws Exception {
        String completedBungId = "a1234567-89ab-cdef-0123-456789abcdef";
        String reviewerUserId = "9e1bfc60-f76a-47dc-9147-803653707192";
        String validTargetUserId = "91b4928f-8288-44dc-a04d-640911f0b2be";
        String invalidUserId = "00000000-0000-0000-0000-000000000000";

        mockMvc.perform(
            patch("/v1/bungs/" + completedBungId + "/complete")
                .header(AUTH_HEADER, getTestUserToken2())
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

        var feedbackRequest = new HashMap<>();
        feedbackRequest.put("bungId", completedBungId);
        feedbackRequest.put("targetUserIds", List.of(validTargetUserId, invalidUserId));

        mockMvc.perform(
            patch(PREFIX + "/feedback")
                .header(AUTH_HEADER, getTestUserToken1())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonify(feedbackRequest))
        ).andExpect(status().isNotFound());

        entityManager.flush();
        entityManager.clear();

        Integer feedback = jdbcTemplate.queryForObject(
            "SELECT feedback FROM tb_users WHERE user_id = ?",
            Integer.class,
            validTargetUserId
        );
        Boolean submitted = jdbcTemplate.queryForObject(
            """
                SELECT feedback_submitted_at IS NOT NULL
                FROM tb_users_bungs
                WHERE bung_id = ? AND user_id = ?
                """,
            Boolean.class,
            completedBungId,
            reviewerUserId
        );

        assertThat(feedback).isZero();
        assertThat(submitted).isFalse();
    }

    @Test
    @DisplayName("완료되지 않은 벙에는 피드백을 저장할 수 없다")
    @Transactional
    void increaseFeedback_uncompletedBungDoesNotSubmit() throws Exception {
        String notCompletedBungId = "c0477004-1632-455f-acc9-04584b55921f";
        String reviewerUserId = "9e1bfc60-f76a-47dc-9147-803653707192";
        String targetUserId = "91b4928f-8288-44dc-a04d-640911f0b2be";

        var feedbackRequest = new HashMap<>();
        feedbackRequest.put("bungId", notCompletedBungId);
        feedbackRequest.put("targetUserIds", List.of(targetUserId));

        mockMvc.perform(
            patch(PREFIX + "/feedback")
                .header(AUTH_HEADER, getTestUserToken1())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonify(feedbackRequest))
        ).andExpect(status().isConflict());

        entityManager.flush();
        entityManager.clear();

        Integer feedback = jdbcTemplate.queryForObject(
            "SELECT feedback FROM tb_users WHERE user_id = ?",
            Integer.class,
            targetUserId
        );
        Boolean submitted = jdbcTemplate.queryForObject(
            """
                SELECT feedback_submitted_at IS NOT NULL
                FROM tb_users_bungs
                WHERE bung_id = ? AND user_id = ?
                """,
            Boolean.class,
            notCompletedBungId,
            reviewerUserId
        );

        assertThat(feedback).isZero();
        assertThat(submitted).isFalse();
    }

    @Test
    @DisplayName("같은 벙 피드백을 다시 저장해도 피드백 수는 한 번만 증가한다")
    @Transactional
    void increaseFeedback_duplicateSubmissionDoesNotIncrementAgain() throws Exception {
        String completedBungId = "a1234567-89ab-cdef-0123-456789abcdef";
        String targetUserId = "91b4928f-8288-44dc-a04d-640911f0b2be";

        mockMvc.perform(
            patch("/v1/bungs/" + completedBungId + "/complete")
                .header(AUTH_HEADER, getTestUserToken2())
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

        var feedbackRequest = new HashMap<>();
        feedbackRequest.put("bungId", completedBungId);
        feedbackRequest.put("targetUserIds", List.of(targetUserId));

        mockMvc.perform(
            patch(PREFIX + "/feedback")
                .header(AUTH_HEADER, getTestUserToken1())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonify(feedbackRequest))
        ).andExpect(status().isOk());

        mockMvc.perform(
            patch(PREFIX + "/feedback")
                .header(AUTH_HEADER, getTestUserToken1())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonify(feedbackRequest))
        ).andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        Integer feedback = jdbcTemplate.queryForObject(
            "SELECT feedback FROM tb_users WHERE user_id = ?",
            Integer.class,
            targetUserId
        );

        assertThat(feedback).isOne();
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
