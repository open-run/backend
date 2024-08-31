package io.openur.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.openur.config.TestSupport;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class BungApiTest extends TestSupport {
    private static final String PREFIX = "/v1/bungs";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("벙 생성")
    @Transactional
    void createBungTest() throws Exception {
        String token = getTestUserToken("test1@test.com");

        var submittedBung = new HashMap<>();
        submittedBung.put("name", "이름");
        submittedBung.put("description", "설명");
        submittedBung.put("location", "장소");
        submittedBung.put("startDateTime", LocalDateTime.now().toString());
        submittedBung.put("endDateTime", LocalDateTime.now().plusDays(1).toString());
        submittedBung.put("distance", "10.5");
        submittedBung.put("pace", "5'55\"");
        submittedBung.put("memberNumber", 5);
        submittedBung.put("hasAfterRun", false);
        submittedBung.put("afterRunDescription", "");

        mockMvc.perform(
            post(PREFIX)
                .header(AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonify(submittedBung))
        ).andExpect(status().isCreated());
    }

    @Nested
    @DisplayName("벙 목록")
    class getBungList {
        String token = getTestUserToken("test2@test.com");

        @Test
        @DisplayName("전체 보기")
        void getBungListAll() throws Exception {
            mockMvc.perform(
                get(PREFIX)
                    .header(AUTH_HEADER, token)
            ).andExpect(status().isOk());
        }

        @Test
        @DisplayName("참여한 벙 보기")
        void getBungListJoined() throws Exception {
            mockMvc.perform(
                get(PREFIX)
                    .header(AUTH_HEADER, token)
            ).andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("벙 상세 정보")
    void getBungDetail() throws Exception {
        String token = getTestUserToken("test1@test.com");
        String bungId = "c0477004-1632-455f-acc9-04584b55921f";

        mockMvc.perform(
            get(PREFIX + "/{bungId}", bungId)
                .header(AUTH_HEADER, token)
            )
            .andExpect(status().isOk());
    }

    @Nested
    @DisplayName("벙 삭제")
    class deleteBungTest {

        String bungId = "c0477004-1632-455f-acc9-04584b55921f";

        @Test
        @DisplayName("403 Forbidden. Authorization Header 없음")
        @Transactional
        void deleteBung_isForbidden() throws Exception {
            mockMvc.perform(
                    delete(PREFIX + "/" + bungId)
                )
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("403 Forbidden. Bung owner 가 아닌 경우")
        @Transactional
        void deleteBung_isForbidden_notOwner() throws Exception {
            String notOwnerToken = getTestUserToken("test2@test.com");
            mockMvc.perform(
                delete(PREFIX + "/" + bungId)
                    .header(AUTH_HEADER, notOwnerToken)
            ).andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("401 Unauthorized. invalid Authorization Header")
        @Transactional
        void deleteBung_isUnauthorized() throws Exception {
            String invalidToken = "Bearer invalidToken";
            mockMvc.perform(
                delete(PREFIX + "/" + bungId)
                    .header(AUTH_HEADER, invalidToken)
            ).andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("401 Unauthorized. Unknown user token")
        @Transactional
        void deleteBung_isUnauthorized_unknownUser() throws Exception {
            String unknownUserToken = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ5ZWppbmtlbGx5am9vQGdtYWlsLmNvbSIsImV4cCI6MTcyMzYyNDgxMCwiaWF0IjoxNzIzNjIxMjEwfQ.wH-eJCvEBgFg_QjWr7CdxBpMqlQzGt45DLmrsWju-HU";
            mockMvc.perform(
                delete(PREFIX + "/" + bungId)
                    .header(AUTH_HEADER, unknownUserToken)
            ).andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("202 Accepted.")
        @Transactional
        void deleteBung_isAccepted() throws Exception {
            String token = getTestUserToken("test1@test.com");
            mockMvc.perform(
                delete(PREFIX + "/" + bungId)
                    .header(AUTH_HEADER, token)
            ).andExpect(status().isAccepted());
        }

    }

    @Nested
    class getOwnedBungDetailsTest {
        @Test
        @DisplayName("Bung : 내가 소유한 벙 정보 조회 테스트")
        void getOwnedBungDetails_isOkTest() throws Exception {
            String token = getTestUserToken("test1@test.com");

            MvcResult result = mockMvc.perform(
                    get(PREFIX + "/my-bungs")
                        .header(AUTH_HEADER, token)
                        .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andReturn();

            String responseBody = result.getResponse().getContentAsString();
            System.out.println("Response Body: " + responseBody);

            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode dataNode = rootNode.path("data");

            List<String> bungIds = StreamSupport.stream(dataNode.spliterator(), false)
                .map(node -> node.path("bungId").asText())
                .collect(Collectors.toList());

            List<String> expectedBungIds = List.of(
                "c0477004-1632-455f-acc9-04584b55921f",
                "c0477004-1632-455f-acc9-04584b67123f",
                "c1422356-1332-465c-abc9-04574c99921c"
            );

            assertThat(bungIds).containsExactlyInAnyOrderElementsOf(expectedBungIds);
        }


        @Test
        @DisplayName("Bung : 내가 벙주인 벙 정보 조회 실패 테스트 - Authorization Header 없음")
        void getOwnedBungDetails_unauthorizedTest() throws Exception {
            mockMvc.perform(
                get(PREFIX + "/my-bungs")
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isForbidden());
        }
    }
}
