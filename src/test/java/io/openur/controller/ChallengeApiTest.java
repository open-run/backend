package io.openur.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import io.openur.config.TestSupport;
import io.openur.domain.challenge.dto.GeneralChallengeDto;
import io.openur.domain.challenge.dto.RepetitiveChallengeDto;
import io.openur.domain.challenge.dto.RepetitiveChallengeTreeDto;
import io.openur.global.dto.PagedResponse;
import io.openur.global.dto.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ChallengeApiTest extends TestSupport {

    private static final String PREFIX = "/v1/challenges";
    private static final String TEST_USER1_ID = "9e1bfc60-f76a-47dc-9147-803653707192";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Nested
    @DisplayName("내 챌린지 목록 조회 - 조건별 테스트")
    class getMyChallengeListTest {

        @Test
        @DisplayName("200 OK - 보상 수령 가능한 일반 챌린지는 진행 목록에 노출된다")
        void getGeneralChallengeList_rewardableChallenge_isOk() throws Exception {
            // given: user1은 normal challenge 1을 달성했고 아직 NFT 보상은 받지 않음
            String token = getTestUserToken1();

            // when
            MvcResult result = mockMvc.perform(
                get(PREFIX + "/general")
                    .header(AUTH_HEADER, token)
                    .param("page", "0")
                    .param("limit", "10")
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()).andReturn();

            // then
            PagedResponse<GeneralChallengeDto> response = parseResponse(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {}
            );

            assertNotNull(response);
            assertThat(response.getData()).hasSize(1);
            GeneralChallengeDto first = response.getData().get(0);
            assertThat(first.getChallengeId()).isEqualTo(1L);
            assertThat(first.getUserChallengeId()).isEqualTo(1L);
            assertThat(first.getCompletedDate()).isNotNull();
            assertThat(first.isNftCompleted()).isFalse();
            assertThat(first.isAccomplished()).isTrue();
            assertThat(response.isFirst()).isTrue();
            assertThat(response.isLast()).isTrue();
        }

        @Test
        @DisplayName("200 OK - 완료된 챌린지 빈 결과 (user2 has no ucs)")
        void getCompletedChallengeList_emptyResult_isOk() throws Exception {
            // given: user2는 user_challenge가 0개라서 NFT 발급 대기 완료 challenge가 없음
            String token = getTestUserToken2();

            // when
            MvcResult result = mockMvc.perform(
                get(PREFIX + "/completed")
                    .header(AUTH_HEADER, token)
                    .param("page", "0")
                    .param("limit", "10")
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()).andReturn();

            // then
            PagedResponse<GeneralChallengeDto> response = parseResponse(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {}
            );

            assertNotNull(response);
            assertThat(response.getData()).isEmpty();
            assertThat(response.getTotalElements()).isEqualTo(0);
            assertThat(response.getMessage()).isEqualTo("success");
        }

        @Test
        @DisplayName("200 OK - 신규 유저도 일반 challenge 마스터 목록을 모두 본다 (progress 0%)")
        void getGeneralChallengeList_newUserSeesAllNormalChallenges_isOk() throws Exception {
            // given: user2는 user_challenge가 하나도 없는 신규 유저
            String token = getTestUserToken2();

            // when
            MvcResult result = mockMvc.perform(
                get(PREFIX + "/general")
                    .header(AUTH_HEADER, token)
                    .param("page", "0")
                    .param("limit", "10")
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()).andReturn();

            // then: tb_challenges에 normal 1개(challenge_id=1, stage 1)가 있으므로 1건
            PagedResponse<GeneralChallengeDto> response = parseResponse(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {}
            );

            assertNotNull(response);
            assertThat(response.getData()).hasSize(1);
            GeneralChallengeDto first = response.getData().get(0);
            assertThat(first.getChallengeId()).isEqualTo(1L);
            assertThat(first.getStageCount()).isEqualTo(1);
            assertThat(first.getCurrentCount()).isEqualTo(0);
            assertThat(first.getProgressStat()).isEqualTo(0.0f);
            assertThat(first.isAccomplished()).isFalse();
            assertThat(first.getUserChallengeId()).isNull();
        }

        @Test
        @DisplayName("200 OK - 신규 유저도 반복 challenge 마스터 목록(stage 1)을 본다")
        void getRepetitiveChallengeList_newUserSeesAllRepetitiveChallenges_isOk() throws Exception {
            // given
            String token = getTestUserToken2();

            // when
            MvcResult result = mockMvc.perform(
                get(PREFIX + "/repetitive")
                    .header(AUTH_HEADER, token)
                    .param("page", "0")
                    .param("limit", "10")
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()).andReturn();

            // then: tb_challenges에 repetitive 1개(challenge_id=2)이고 stage 1이 노출
            PagedResponse<GeneralChallengeDto> response = parseResponse(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {}
            );

            assertNotNull(response);
            assertThat(response.getData()).hasSize(1);
            GeneralChallengeDto first = response.getData().get(0);
            assertThat(first.getChallengeId()).isEqualTo(2L);
            assertThat(first.getStageCount()).isEqualTo(1); // 최소 stage_number는 1
            assertThat(first.getCurrentCount()).isEqualTo(0);
            assertThat(first.getProgressStat()).isEqualTo(0.0f);
            assertThat(first.isAccomplished()).isFalse();
            assertThat(first.getUserChallengeId()).isNull();
        }

        @Test
        @DisplayName("200 OK - 신규 유저 반복 상세 조회 시 모든 stage가 nullable userChallenge로 노출")
        void getRepetitiveChallengeDetail_newUser_isOk() throws Exception {
            // given: user2는 challenge 2에 대해 user_challenge가 없는 상태
            String token = getTestUserToken2();
            Long challengeId = 2L;

            // when: 예전에는 ChallengeNotAssignedException을 던졌으나 이제 200 + 빈 진척도
            MvcResult result = mockMvc.perform(
                get(PREFIX + "/repetitive/{challengeId}", challengeId)
                    .header(AUTH_HEADER, token)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()).andReturn();

            // then
            Response<RepetitiveChallengeTreeDto> response = parseResponse(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {}
            );

            assertNotNull(response);
            assertThat(response.getMessage()).isEqualTo("success");
            RepetitiveChallengeTreeDto tree = response.getData();
            assertNotNull(tree);
            assertThat(tree.getChallengeId()).isEqualTo(challengeId);
            assertThat(tree.getChallengeTrees()).hasSize(3); // stage 1, 2, 3 모두 노출
            for (RepetitiveChallengeDto stage : tree.getChallengeTrees()) {
                assertThat(stage.getUserChallengeId()).isNull();
                assertThat(stage.getCurrentCount()).isEqualTo(0);
                assertThat(stage.getCompletedDate()).isNull();
                assertThat(stage.isNftCompleted()).isFalse();
            }
        }

        @Test
        @DisplayName("200 OK - 보상 수령 가능한 반복 챌린지는 진행 목록에 노출된다")
        void getRepetitiveChallengeList_rewardableChallenge_isOk() throws Exception {
            // given: user1은 challenge 2의 stage 2를 달성했고 아직 NFT 보상은 받지 않음
            String token = getTestUserToken1();

            // when
            MvcResult result = mockMvc.perform(
                get(PREFIX + "/repetitive")
                    .header(AUTH_HEADER, token)
                    .param("page", "0")
                    .param("limit", "10")
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()).andReturn();

            // then
            PagedResponse<GeneralChallengeDto> response = parseResponse(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {}
            );

            assertNotNull(response);
            assertThat(response.getData()).hasSize(1);
            GeneralChallengeDto first = response.getData().get(0);
            assertThat(first.getChallengeId()).isEqualTo(2L);
            assertThat(first.getUserChallengeId()).isEqualTo(2L);
            assertThat(first.getCompletedDate()).isNotNull();
            assertThat(first.isNftCompleted()).isFalse();
            assertThat(first.isAccomplished()).isTrue();
            assertThat(response.getMessage()).isEqualTo("success");
        }

        @Test
        @DisplayName("200 OK - NFT 보상까지 완료한 도전과제는 진행 목록에서 제외된다")
        void getChallengeLists_nftCompletedChallenge_isHidden() throws Exception {
            // given
            jdbcTemplate.update(
                "UPDATE tb_users_challenges SET nft_completed = TRUE WHERE user_challenge_id IN (1, 2)");
            String token = getTestUserToken1();

            // when
            MvcResult generalResult = mockMvc.perform(
                get(PREFIX + "/general")
                    .header(AUTH_HEADER, token)
                    .param("page", "0")
                    .param("limit", "10")
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()).andReturn();
            MvcResult repetitiveResult = mockMvc.perform(
                get(PREFIX + "/repetitive")
                    .header(AUTH_HEADER, token)
                    .param("page", "0")
                    .param("limit", "10")
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()).andReturn();

            // then
            PagedResponse<GeneralChallengeDto> generalResponse = parseResponse(
                generalResult.getResponse().getContentAsString(),
                new TypeReference<>() {}
            );
            PagedResponse<GeneralChallengeDto> repetitiveResponse = parseResponse(
                repetitiveResult.getResponse().getContentAsString(),
                new TypeReference<>() {}
            );

            assertThat(generalResponse.getData()).isEmpty();
            assertThat(repetitiveResponse.getData()).isEmpty();
        }

        @Test
        @DisplayName("200 OK - 보상 수령 가능 stage는 다음 진행 stage보다 우선 노출된다")
        void getRepetitiveChallengeList_rewardableStage_hasPriority() throws Exception {
            // given: stage 2는 보상 수령 가능, stage 3은 다음 진행 stage
            jdbcTemplate.update(
                """
                    INSERT INTO tb_users_challenges
                        (user_challenge_id, user_id, challenge_stage_id, current_count,
                         current_progress, nft_completed, completed_date)
                    VALUES
                        (10, ?, 4, 3, 60.0, FALSE, NULL)
                    """,
                TEST_USER1_ID
            );
            String token = getTestUserToken1();

            // when
            MvcResult result = mockMvc.perform(
                get(PREFIX + "/repetitive")
                    .header(AUTH_HEADER, token)
                    .param("page", "0")
                    .param("limit", "10")
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()).andReturn();

            // then
            PagedResponse<GeneralChallengeDto> response = parseResponse(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {}
            );

            assertThat(response.getData()).hasSize(1);
            GeneralChallengeDto first = response.getData().get(0);
            assertThat(first.getChallengeId()).isEqualTo(2L);
            assertThat(first.getUserChallengeId()).isEqualTo(2L);
            assertThat(first.getStageCount()).isEqualTo(2);
            assertThat(first.getCompletedDate()).isNotNull();
            assertThat(first.isNftCompleted()).isFalse();
        }

        @Test
        @DisplayName("200 OK - 일반 챌린지 목록 조회 (user2: 신규 유저 케이스 = 1건)")
        void getGeneralChallengeList_isOk() throws Exception {
            // given
            String token = getTestUserToken2();

            // when
            MvcResult result = mockMvc.perform(
                get(PREFIX + "/general")
                    .header(AUTH_HEADER, token)
                    .param("page", "0")
                    .param("limit", "10")
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()).andReturn();

            // then
            PagedResponse<GeneralChallengeDto> response = parseResponse(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {}
            );

            assertNotNull(response);
            assertThat(response.getMessage()).isEqualTo("success");
            assertThat(response.getData().size()).isLessThanOrEqualTo(10);
        }

        @Test
        @DisplayName("200 OK - 완료된 챌린지 목록 조회")
        void getCompletedChallengeList_isOk() throws Exception {
            // given: user1은 challenge 1을 완료했고 nft_completed=false
            String token = getTestUserToken1();

            // when
            MvcResult result = mockMvc.perform(
                get(PREFIX + "/completed")
                    .header(AUTH_HEADER, token)
                    .param("page", "0")
                    .param("limit", "10")
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()).andReturn();

            // then
            PagedResponse<GeneralChallengeDto> response = parseResponse(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {}
            );

            assertNotNull(response);
            assertThat(response.getMessage()).isEqualTo("success");
            assertThat(response.getData().size()).isLessThanOrEqualTo(10);
        }

        @Test
        @DisplayName("200 OK - 반복 챌린지 목록 조회 (user2: 신규 유저 = stage 1)")
        void getRepetitiveChallengeList_isOk() throws Exception {
            // given
            String token = getTestUserToken2();

            // when
            MvcResult result = mockMvc.perform(
                get(PREFIX + "/repetitive")
                    .header(AUTH_HEADER, token)
                    .param("page", "0")
                    .param("limit", "10")
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()).andReturn();

            // then
            PagedResponse<GeneralChallengeDto> response = parseResponse(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {}
            );

            assertNotNull(response);
            assertThat(response.getMessage()).isEqualTo("success");
            assertThat(response.getData().size()).isLessThanOrEqualTo(10);
        }

        @Test
        @DisplayName("200 OK - 반복 챌린지 상세 조회 (user1: 진행 중)")
        void getRepetitiveChallengeDetail_isOk() throws Exception {
            // given: user1은 challenge 2의 stage 2 완료 상태
            String token = getTestUserToken1();
            Long challengeId = 2L;

            // when
            MvcResult result = mockMvc.perform(
                get(PREFIX + "/repetitive/{challengeId}", challengeId)
                    .header(AUTH_HEADER, token)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()).andReturn();

            // then
            Response<RepetitiveChallengeTreeDto> response = parseResponse(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {}
            );

            assertNotNull(response);
            assertThat(response.getMessage()).isEqualTo("success");
            assertNotNull(response.getData());
        }
    }
}
