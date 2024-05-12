package io.openur.controller;

import io.openur.config.TestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.HashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerTest extends TestSupport {
    private static final String PREFIX = "/v1/users";

    @Test
    @DisplayName("User : 닉네임 중복확인 테스트")
    void getExistNicknameTest() throws Exception {
        String testName = "zXc9U1i01";

        mockMvc.perform(
            get(PREFIX + "/nickname/exist?nickname={nickname}", testName)
                .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("User : 유저 정보 테스트")
    void getUserInfoTest() throws Exception {
        String token = getTestUserToken();

        mockMvc.perform(
            get(PREFIX)
                .header(AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("User : 페이스 기록 입력 저장")
    void saveSurveyResultTest() throws Exception {
        String token = getTestUserToken();
        var surveyResult = new HashMap<>();
        surveyResult.put("nickname", "나도모른거");
        surveyResult.put("runningPace", "5'55\"");
        surveyResult.put("runningFrequency", "2");

        mockMvc.perform(
            patch(PREFIX)
                .header(AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonify(surveyResult))
            )
            .andExpect(status().isCreated());
    }
}
