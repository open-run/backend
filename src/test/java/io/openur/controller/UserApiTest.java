package io.openur.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import io.openur.config.TestSupport;
import io.openur.domain.user.dto.GetUserResponseDto;
import io.openur.global.common.Response;
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
            .andExpect(status().isOk());
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
