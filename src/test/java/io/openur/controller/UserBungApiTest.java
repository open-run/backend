package io.openur.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import io.openur.config.TestSupport;
import io.openur.domain.userbung.model.UserBung;
import io.openur.domain.userbung.repository.UserBungRepositoryImpl;
import io.openur.global.dto.ExceptionDto;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class UserBungApiTest extends TestSupport {

    private static final String PREFIX = "/v1/bungs";
    @Autowired
    protected UserBungRepositoryImpl userBungRepository;

    @Nested
    @DisplayName("멤버 제거")
    class kickMemberTest {

        @Test
        @DisplayName("200 Ok. Is bung owner.")
        void kickMember_successTest() throws Exception {
            String token = getTestUserToken("test1@test.com");
            String bungId = "c0477004-1632-455f-acc9-04584b55921f";
            String userIdToKick = "91b4928f-8288-44dc-a04d-640911f0b2be";

            mockMvc.perform(
                delete(PREFIX + "/{bungId}/members/{userIdToKick}", bungId, userIdToKick)
                    .header(AUTH_HEADER, token)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk());
        }

        @Test
        @DisplayName("200 Ok. Is self.")
        void kickMember_success_isSelf() throws Exception {
            String token = getTestUserToken("test2@test.com");
            String bungId = "c0477004-1632-455f-acc9-04584b55921f";
            String userIdToKick = "91b4928f-8288-44dc-a04d-640911f0b2be";

            mockMvc.perform(
                delete(PREFIX + "/{bungId}/members/{userIdToKick}", bungId, userIdToKick)
                    .header(AUTH_HEADER, token)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk());

            assertThatThrownBy(() -> userBungRepository.findByUserIdAndBungId(userIdToKick, bungId))
                .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("403 Forbidden. Owner is trying to kick self.")
        void kickMember_isForbidden_ownerKickingSelf() throws Exception {
            String token = getTestUserToken("test1@test.com");
            String bungId = "c0477004-1632-455f-acc9-04584b55921f";
            String userIdToKick = "9e1bfc60-f76a-47dc-9147-803653707192";

            MvcResult result = mockMvc.perform(
                delete(PREFIX + "/{bungId}/members/{userIdToKick}", bungId, userIdToKick)
                    .header(AUTH_HEADER, token)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isForbidden()).andReturn();

            ExceptionDto response = parseResponse(
                result.getResponse().getContentAsString(), new TypeReference<>() {
                });
            assert Objects.equals(response.getMessage(),
                "Owners cannot remove themselves from bung.");
        }

        @Test
        @DisplayName("403 Forbidden. Is not owner nor self.")
        void kickMember_isForbidden_notOwnerNorSelf() throws Exception {
            String token = getTestUserToken("test3@test.com");
            String bungId = "c0477004-1632-455f-acc9-04584b55921f";
            String userIdToKick = "91b4928f-8288-44dc-a04d-640911f0b2be";

            MvcResult result = mockMvc.perform(
                delete(PREFIX + "/{bungId}/members/{userIdToKick}", bungId, userIdToKick)
                    .header(AUTH_HEADER, token)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isForbidden()).andReturn();

            ExceptionDto response = parseResponse(
                result.getResponse().getContentAsString(), new TypeReference<>() {
                });
            assert Objects.equals(response.getMessage(),
                "Must be the owner of bung or self to remove user from bung.");
        }

    }

    @Nested
    @DisplayName("벙주 변경")
    class changeOwnerTest {

        @Test
        @DisplayName("200 Ok.")
        void changeOwner_isOkTest() throws Exception {
            String token = getTestUserToken("test1@test.com");

            String bungId = "c0477004-1632-455f-acc9-04584b55921f";
            String newOwnerUserId = "91b4928f-8288-44dc-a04d-640911f0b2be";
            String oldOwnerUserId = "9e1bfc60-f76a-47dc-9147-803653707192";

            mockMvc.perform(
                patch(PREFIX + "/{bungId}/change-owner?newOwnerUserId={newOwnerUserId}", bungId,
                    newOwnerUserId)
                    .header(AUTH_HEADER, token)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk());

            UserBung newOwnerBung = userBungRepository.findByUserIdAndBungId(newOwnerUserId,
                bungId);
            assertThat(newOwnerBung).isNotNull();
            assertThat(newOwnerBung.isOwner()).isTrue();

            UserBung oldOwnerBung = userBungRepository.findByUserIdAndBungId(oldOwnerUserId,
                bungId);
            assertThat(oldOwnerBung).isNotNull();
            assertThat(oldOwnerBung.isOwner()).isFalse();
        }

        @Test
        @DisplayName("403 Forbidden. Authorization Header 없음")
        void changeOwner_isForbidden() throws Exception {
            String bungId = "c0477004-1632-455f-acc9-04584b55921f";
            String newOwnerUserId = "91b4928f-8288-44dc-a04d-640911f0b2be";

            mockMvc.perform(
                patch(PREFIX + "/{bungId}/change-owner?newOwnerUserId={newOwnerUserId}", bungId,
                    newOwnerUserId)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("403 Forbidden. Bung owner 가 아닌 경우")
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

        @Test
        @DisplayName("401 Unauthorized. Unknown user token")
        void changeOwner_isUnauthorized_unknownUser() throws Exception {
            String unknownUserToken = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ5ZWppbmtlbGx5am9vQGdtYWlsLmNvbSIsImV4cCI6MTcyMzYyNDgxMCwiaWF0IjoxNzIzNjIxMjEwfQ.wH-eJCvEBgFg_QjWr7CdxBpMqlQzGt45DLmrsWju-HU";

            String bungId = "c0477004-1632-455f-acc9-04584b55921f";
            String newOwnerUserId = "91b4928f-8288-44dc-a04d-640911f0b2be";

            mockMvc.perform(
                patch(PREFIX + "/{bungId}/change-owner?newOwnerUserId={newOwnerUserId}", bungId,
                    newOwnerUserId)
                    .header(AUTH_HEADER, unknownUserToken)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("벙 참여 인증 완료")
    class confirmBungParticipationTest {

        private void isOkTest(String token, String userId, String bungId) throws Exception {
            mockMvc.perform(
                patch(PREFIX + "/{bungId}/participated", bungId)
                    .header(AUTH_HEADER, token)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk());

            UserBung userBung = userBungRepository.findByUserIdAndBungId(userId, bungId);
            assertThat(userBung.isParticipationStatus()).isTrue();
        }

        @Test
        @DisplayName("200 Ok. 벙주인 경우")
        void confirmBungParticipation_isOkTest() throws Exception {
            String token = getTestUserToken("test1@test.com");
            String userId = "9e1bfc60-f76a-47dc-9147-803653707192";
            String bungId = "c0477004-1632-455f-acc9-04584b55921f";

            isOkTest(token, userId, bungId);
        }

        @Test
        @DisplayName("200 Ok. 벙 참가자인 경우")
        void confirmBungParticipation_isOk_participant() throws Exception {
            String token = getTestUserToken("test2@test.com");
            String userId = "91b4928f-8288-44dc-a04d-640911f0b2be";
            String bungId = "c0477004-1632-455f-acc9-04584b55921f";

            isOkTest(token, userId, bungId);
        }

        @Test
        @DisplayName("403 Forbidden. 벙 참가자가 아닌 경우")
        void confirmBungParticipation_isForbidden_notParticipant() throws Exception {
            String token = getTestUserToken("test3@test.com");
            String bungId = "c0477004-1632-455f-acc9-04584b55921f";

            mockMvc.perform(
                patch(PREFIX + "/{bungId}/participated", bungId)
                    .header(AUTH_HEADER, token)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isForbidden());
        }
    }
}
