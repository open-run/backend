package io.openur.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import io.openur.config.TestSupport;
import io.openur.domain.userbung.entity.UserBungEntity;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class BungApiTest extends TestSupport {
    private static final String PREFIX = "/v1/bungs";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Bung : 벙 생성 테스트")
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
    class changeOwnerTest {
        @Test
        @DisplayName("Bung : 벙주 변경 테스트")
        void changeOwner_isOkTest() throws Exception {
            String token = getTestUserToken("test1@test.com");

            String bungId = "c0477004-1632-455f-acc9-04584b55921f";
            String newOwnerUserId = "91b4928f-8288-44dc-a04d-640911f0b2be";
            String oldOwnerUserId = "9e1bfc60-f76a-47dc-9147-803653707192";

            mockMvc.perform(
                patch(PREFIX + "/{bungId}/change-owner?newOwnerUserId={newOwnerUserId}", bungId, newOwnerUserId)
                    .header(AUTH_HEADER, token)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk());

            Optional<UserBungEntity> newOwnerBung = userBungJpaRepository
                .findByUserEntity_UserIdAndBungEntity_BungId(newOwnerUserId, bungId);
            assertThat(newOwnerBung).isPresent();
            assertThat(newOwnerBung.get().isOwner()).isTrue();

            Optional<UserBungEntity> oldOwnerBung = userBungJpaRepository
                .findByUserEntity_UserIdAndBungEntity_BungId(oldOwnerUserId, bungId);
            assertThat(oldOwnerBung).isPresent();
            assertThat(oldOwnerBung.get().isOwner()).isFalse();
        }

// mockMvc 가 PreAuthorize interceptor를 bypass 해버려서 원하는 상황의 테스트 실행이 불가능함
//        @Test
//        @DisplayName("벙주 변경 실패 - Authorization Header 없음. 403 Forbidden")
//        void changeOwner_forbiddenTest() throws Exception {
//            String bungId = "c0477004-1632-455f-acc9-04584b55921f";
//            String newOwnerUserId = "91b4928f-8288-44dc-a04d-640911f0b2be";
//
//            mockMvc.perform(
//                patch(PREFIX + "/{bungId}/change-owner?newOwnerUserId={newOwnerUserId}", bungId, newOwnerUserId)
//                    .contentType(MediaType.APPLICATION_JSON)
//            ).andExpect(status().isForbidden());
//        }
//
//        @Test
//        @DisplayName("벙주 변경 실패 - 잘못된 Authorization Header 401 Unauthorized")
//        void changeOwner_unauthorizedTest() throws Exception {
//            String invalidToken = getTestUserToken("test2@test.com");
//
//            String bungId = "c0477004-1632-455f-acc9-04584b55921f";
//            String newOwnerUserId = "91b4928f-8288-44dc-a04d-640911f0b2be";
//
//            mockMvc.perform(
//                patch(PREFIX + "/{bungId}/change-owner?newOwnerUserId={newOwnerUserId}", bungId, newOwnerUserId)
//                    .header(AUTH_HEADER, invalidToken)
//                    .contentType(MediaType.APPLICATION_JSON)
//            ).andExpect(status().isUnauthorized());
//        }
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
                "c0477004-1632-455f-acc9-04584b55921f"//,
                //"c1422356-1332-465c-abc9-04574c99921c"
            );

            assertThat(bungIds).containsExactlyInAnyOrderElementsOf(expectedBungIds);
        }


        @Test
        @DisplayName("Bung : 내가 벙주인 벙 정보 조회 실패 테스트 - Authorization Header 없음")
        void getOwnedBungDetails_unauthorizedTest() throws Exception {
            mockMvc.perform(
                get(PREFIX + "/my-bungs")
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isUnauthorized());
        }
    }

}
