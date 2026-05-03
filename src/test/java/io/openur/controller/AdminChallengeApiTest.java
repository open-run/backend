package io.openur.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.openur.config.TestSupport;
import io.openur.domain.NFT.service.NftMintClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class AdminChallengeApiTest extends TestSupport {

    private static final String PREFIX = "/v1/admin/challenges";

    @MockBean
    private NftMintClient nftMintClient;

    @Test
    @DisplayName("admin은 도전과제 목록과 stage 연결 상태를 조회한다")
    void getAdminChallenges_returnsStagesAndAssignmentCounts() throws Exception {
        mockMvc.perform(get(PREFIX)
                .header(AUTH_HEADER, getTestUserToken1()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Admin challenges fetched successfully"))
            .andExpect(jsonPath("$.data", hasSize(2)))
            .andExpect(jsonPath("$.data[0].challengeId").value(1))
            .andExpect(jsonPath("$.data[0].name").value("test_challenge"))
            .andExpect(jsonPath("$.data[0].assignedUserChallengeCount").value(1))
            .andExpect(jsonPath("$.data[0].deletable").value(false))
            .andExpect(jsonPath("$.data[0].stages", hasSize(1)))
            .andExpect(jsonPath("$.data[0].stages[0].stageId").value(1))
            .andExpect(jsonPath("$.data[0].stages[0].assignedUserChallengeCount").value(1))
            .andExpect(jsonPath("$.data[0].stages[0].removable").value(false))
            .andExpect(jsonPath("$.data[1].stages", hasSize(3)));
    }

    @Test
    @DisplayName("admin은 도전과제와 stage를 생성한다")
    void createAdminChallenge_createsChallengeWithStages() throws Exception {
        mockMvc.perform(post(PREFIX)
                .header(AUTH_HEADER, getTestUserToken1())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCreateRequest()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Admin challenge created successfully"))
            .andExpect(jsonPath("$.data.challengeId").isNumber())
            .andExpect(jsonPath("$.data.name").value("admin_created_challenge"))
            .andExpect(jsonPath("$.data.challengeType").value("normal"))
            .andExpect(jsonPath("$.data.rewardType").value("top"))
            .andExpect(jsonPath("$.data.completedType").value("count"))
            .andExpect(jsonPath("$.data.assignedUserChallengeCount").value(0))
            .andExpect(jsonPath("$.data.deletable").value(true))
            .andExpect(jsonPath("$.data.stages", hasSize(2)))
            .andExpect(jsonPath("$.data.stages[0].stageNumber").value(1))
            .andExpect(jsonPath("$.data.stages[0].conditionCount").value(2))
            .andExpect(jsonPath("$.data.stages[0].removable").value(true));
    }

    @Test
    @DisplayName("admin은 도전과제 기본 정보와 stage 조건을 수정한다")
    void updateAdminChallenge_updatesChallengeAndStage() throws Exception {
        mockMvc.perform(put(PREFIX + "/1")
                .header(AUTH_HEADER, getTestUserToken1())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validUpdateRequest()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Admin challenge updated successfully"))
            .andExpect(jsonPath("$.data.challengeId").value(1))
            .andExpect(jsonPath("$.data.name").value("admin_updated_challenge"))
            .andExpect(jsonPath("$.data.description").value("updated_description"))
            .andExpect(jsonPath("$.data.rewardType").value("hair"))
            .andExpect(jsonPath("$.data.stages", hasSize(2)))
            .andExpect(jsonPath("$.data.stages[0].stageId").value(1))
            .andExpect(jsonPath("$.data.stages[0].conditionCount").value(2))
            .andExpect(jsonPath("$.data.stages[1].stageNumber").value(2))
            .andExpect(jsonPath("$.data.stages[1].conditionCount").value(5));
    }

    @Test
    @DisplayName("유저 진행 기록이 없는 stage는 수정 요청에서 제거할 수 있다")
    void updateAdminChallenge_removesUnassignedStage() throws Exception {
        String createResponse = mockMvc.perform(post(PREFIX)
                .header(AUTH_HEADER, getTestUserToken1())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCreateRequest()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        Number challengeId = com.jayway.jsonpath.JsonPath.read(createResponse, "$.data.challengeId");
        Number firstStageId = com.jayway.jsonpath.JsonPath.read(createResponse, "$.data.stages[0].stageId");

        mockMvc.perform(put(PREFIX + "/" + challengeId.longValue())
                .header(AUTH_HEADER, getTestUserToken1())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "admin_created_challenge",
                      "description": "created_description",
                      "challengeType": "normal",
                      "rewardType": "top",
                      "completedType": "count",
                      "conditionDate": null,
                      "conditionText": null,
                      "stages": [
                        {
                          "stageId": %d,
                          "stageNumber": 1,
                          "conditionCount": 2
                        }
                      ]
                    }
                    """.formatted(firstStageId.longValue())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.stages", hasSize(1)))
            .andExpect(jsonPath("$.data.stages[0].stageId").value(firstStageId.longValue()));
    }

    @Test
    @DisplayName("유저 진행 기록이 없는 도전과제는 삭제할 수 있다")
    void deleteAdminChallenge_deletesUnassignedChallenge() throws Exception {
        String createResponse = mockMvc.perform(post(PREFIX)
                .header(AUTH_HEADER, getTestUserToken1())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCreateRequest()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        Number challengeId = com.jayway.jsonpath.JsonPath.read(createResponse, "$.data.challengeId");

        mockMvc.perform(delete(PREFIX + "/" + challengeId.longValue())
                .header(AUTH_HEADER, getTestUserToken1()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Admin challenge deleted successfully"));

        mockMvc.perform(get(PREFIX + "/" + challengeId.longValue())
                .header(AUTH_HEADER, getTestUserToken1()))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("유저 진행 기록이 있는 도전과제는 삭제할 수 없다")
    void deleteAdminChallenge_rejectsAssignedChallenge() throws Exception {
        mockMvc.perform(delete(PREFIX + "/1")
                .header(AUTH_HEADER, getTestUserToken1()))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value(containsString("Cannot delete assigned challenge")));
    }

    @Test
    @DisplayName("유저 진행 기록이 있는 stage는 수정 요청에서 제거할 수 없다")
    void updateAdminChallenge_rejectsRemovingAssignedStage() throws Exception {
        mockMvc.perform(put(PREFIX + "/2")
                .header(AUTH_HEADER, getTestUserToken1())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "test_challenge2",
                      "description": "test_challenge2_description",
                      "challengeType": "repetitive",
                      "rewardType": "face",
                      "completedType": "count",
                      "conditionDate": null,
                      "conditionText": null,
                      "stages": [
                        {
                          "stageId": 2,
                          "stageNumber": 1,
                          "conditionCount": 1
                        },
                        {
                          "stageId": 4,
                          "stageNumber": 3,
                          "conditionCount": 5
                        }
                      ]
                    }
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value(containsString("Cannot remove assigned challenge stage")));
    }

    @Test
    @DisplayName("stage 번호가 중복되면 생성할 수 없다")
    void createAdminChallenge_rejectsDuplicateStageNumber() throws Exception {
        mockMvc.perform(post(PREFIX)
                .header(AUTH_HEADER, getTestUserToken1())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "invalid",
                      "description": "invalid",
                      "challengeType": "normal",
                      "rewardType": "top",
                      "completedType": "count",
                      "conditionDate": null,
                      "conditionText": null,
                      "stages": [
                        { "stageNumber": 1, "conditionCount": 1 },
                        { "stageNumber": 1, "conditionCount": 2 }
                      ]
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("stageNumber must be unique"));
    }

    private String validCreateRequest() {
        return """
            {
              "name": "admin_created_challenge",
              "description": "created_description",
              "challengeType": "normal",
              "rewardType": "top",
              "completedType": "count",
              "conditionDate": null,
              "conditionText": null,
              "stages": [
                {
                  "stageNumber": 1,
                  "conditionCount": 2
                },
                {
                  "stageNumber": 2,
                  "conditionCount": 4
                }
              ]
            }
            """;
    }

    private String validUpdateRequest() {
        return """
            {
              "name": "admin_updated_challenge",
              "description": "updated_description",
              "challengeType": "normal",
              "rewardType": "hair",
              "completedType": "count",
              "conditionDate": null,
              "conditionText": null,
              "stages": [
                {
                  "stageId": 1,
                  "stageNumber": 1,
                  "conditionCount": 2
                },
                {
                  "stageNumber": 2,
                  "conditionCount": 5
                }
              ]
            }
            """;
    }
}
