package io.openur.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.openur.config.TestSupport;
import io.openur.domain.userbung.entity.UserBungEntity;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class BungApiTest extends TestSupport {
    private static final String PREFIX = "/v1/bungs";

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

    @Test
    @DisplayName("회원 제거")
    void kickMember_successTest() throws Exception {
        String token = getTestUserToken("test1@test.com");
        String bungId = "c0477004-1632-455f-acc9-04584b55921f";
        String userIdToKick = "91b4928f-8288-44dc-a04d-640911f0b2be";

        mockMvc.perform(
            delete(PREFIX + "/{bungId}/members/{userIdToKick}", bungId, userIdToKick)
                .header(AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isAccepted());

        Optional<UserBungEntity> kickedUserBung = userBungJpaRepository
            .findByUserEntity_UserIdAndBungEntity_BungId(userIdToKick, bungId);
        assertThat(kickedUserBung).isEmpty();
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
    @DisplayName("벙주 변경")
    class changeOwnerTest {
        @Test
        @DisplayName("200 Ok.")
        @Transactional
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

        @Test
        @DisplayName("403 Forbidden. Authorization Header 없음")
        @Transactional
        void changeOwner_isForbidden() throws Exception {
            String bungId = "c0477004-1632-455f-acc9-04584b55921f";
            String newOwnerUserId = "91b4928f-8288-44dc-a04d-640911f0b2be";

            mockMvc.perform(
                patch(PREFIX + "/{bungId}/change-owner?newOwnerUserId={newOwnerUserId}", bungId, newOwnerUserId)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("403 Forbidden. Bung owner 가 아닌 경우")
        @Transactional
        void changeOwner_isForbidden_notOwner() throws Exception {
            String notOwnerToken = getTestUserToken("test2@test.com");

            String bungId = "c0477004-1632-455f-acc9-04584b55921f";
            String newOwnerUserId = "91b4928f-8288-44dc-a04d-640911f0b2be";

            mockMvc.perform(
                patch(PREFIX + "/{bungId}/change-owner?newOwnerUserId={newOwnerUserId}", bungId,
                    newOwnerUserId)
                    .header(AUTH_HEADER, notOwnerToken)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("401 Unauthorized. invalid Authorization Header")
        @Transactional
        void changeOwner_isUnauthorized() throws Exception {
            String invalidToken = "Bearer invalidToken";

            String bungId = "c0477004-1632-455f-acc9-04584b55921f";
            String newOwnerUserId = "91b4928f-8288-44dc-a04d-640911f0b2be";

            mockMvc.perform(
                patch(PREFIX + "/{bungId}/change-owner?newOwnerUserId={newOwnerUserId}", bungId,
                    newOwnerUserId)
                    .header(AUTH_HEADER, invalidToken)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isUnauthorized());
        }
    }
}
