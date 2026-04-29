package io.openur.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import io.openur.config.TestSupport;
import io.openur.domain.user.dto.GetUserResponseDto;
import io.openur.global.dto.Response;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

public class UserApiTest extends TestSupport {

    private static final String PREFIX = "/v1/users";
    private static final String TEST_USER_ID = "9e1bfc60-f76a-47dc-9147-803653707192";
    private static final byte[] PNG_BYTES = new byte[] {
        (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
        0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
        0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
        0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4,
        (byte) 0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41,
        0x54, 0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00,
        0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte) 0xB4, 0x00,
        0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE,
        0x42, 0x60, (byte) 0x82
    };

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
    @DisplayName("프로필 이미지는 로그인 없이 저장할 수 없다")
    void saveProfileImage_withoutAuthentication_isForbidden() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "profile.png", "image/png", PNG_BYTES);

        mockMvc.perform(
                multipart(PREFIX + "/profile-image")
                    .file(image)
                    .with(request -> {
                        request.setMethod("PUT");
                        return request;
                    })
            )
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("PNG 프로필 이미지를 로컬 에셋 경로에 저장하고 유저 정보에 URL을 반환한다")
    @Transactional
    void saveProfileImage_isOk() throws Exception {
        String token = getTestUserToken1();
        MockMultipartFile image = new MockMultipartFile("image", "profile.png", "image/png", PNG_BYTES);

        mockMvc.perform(
                multipart(PREFIX + "/profile-image")
                    .file(image)
                    .header(AUTH_HEADER, token)
                    .with(request -> {
                        request.setMethod("PUT");
                        return request;
                    })
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.profileImageUrl")
                .value("http://localhost:8080/local-nft-assets/profile-images/users/" + TEST_USER_ID + "/profile.png"));

        assertThat(Files.exists(Path.of(
            "build/test-local-assets/profile-images/users/" + TEST_USER_ID + "/profile.png"
        ))).isTrue();
    }

    @Test
    @DisplayName("PNG가 아닌 프로필 이미지는 저장할 수 없다")
    void saveProfileImage_rejectsNonPng() throws Exception {
        String token = getTestUserToken1();
        MockMultipartFile image = new MockMultipartFile("image", "profile.jpg", "image/jpeg", new byte[] {1, 2, 3});

        mockMvc.perform(
                multipart(PREFIX + "/profile-image")
                    .file(image)
                    .header(AUTH_HEADER, token)
                    .with(request -> {
                        request.setMethod("PUT");
                        return request;
                    })
            )
            .andExpect(status().isBadRequest());
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
