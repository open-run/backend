package io.openur.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import io.openur.config.TestSupport;
import io.openur.domain.challenge.dto.ChallengeInfoDto;
import io.openur.domain.challenge.model.CompletedType;
import io.openur.global.common.PagedResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public class ChallengeApiTest extends TestSupport {

    private static final String PREFIX = "/v1/challenges";

    @Nested
    @DisplayName("내 챌린지 목록 조회 - 조건별 테스트")
    class getMyChallengeListTest {

        @Test
        @DisplayName("200 OK - 조건 미제공 (전체 챌린지 조회)")
        void getMyChallengeList_noTypeProvided_isOk() throws Exception {
            // given
            String token = getTestUserToken1();

            // when
            MvcResult result = mockMvc.perform(
                get(PREFIX)
                    .header(AUTH_HEADER, token)
                    .param("page", "0")
                    .param("limit", "10")
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()).andReturn();

            // then
            PagedResponse<ChallengeInfoDto> response = parseResponse(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {}
            );

            assertNotNull(response);
            assertThat(response.getData()).isNotNull();

            // 모든 타입의 챌린지가 포함되어야 함
            if (!response.getData().isEmpty()) {
                List<CompletedType> foundTypes = response.getData().stream()
                    .map(ChallengeInfoDto::getCompletedType)
                    .distinct()
                    .toList();
                assertThat(foundTypes).isNotEmpty();
            }
        }

        @Test
        @DisplayName("200 OK - date 조건 챌린지 조회")
        void getMyChallengeList_dateType_isOk() throws Exception {
            // given
            String token = getTestUserToken1();
            CompletedType type = CompletedType.date;

            // when
            MvcResult result = mockMvc.perform(
                get(PREFIX)
                    .header(AUTH_HEADER, token)
                    .param("type", type.toString())
                    .param("page", "0")
                    .param("limit", "10")
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()).andReturn();

            // then
            PagedResponse<ChallengeInfoDto> response = parseResponse(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {}
            );

            assertNotNull(response);
            assertThat(response.getData()).isNotNull();

            // date 타입 챌린지만 반환되었는지 검증
            if (!response.getData().isEmpty()) {
                response.getData().forEach(challenge -> {
                    assertThat(challenge.getCompletedType()).isEqualTo(CompletedType.date);
                    assertThat(challenge.getConditionDate()).isNotNull(); // date 조건이 있어야 함
                });
            }
        }

        @Test
        @DisplayName("200 OK - count 조건 챌린지 조회")
        void getMyChallengeList_countType_isOk() throws Exception {
            // given
            String token = getTestUserToken1();
            CompletedType type = CompletedType.count;

            // when
            MvcResult result = mockMvc.perform(
                get(PREFIX)
                    .header(AUTH_HEADER, token)
                    .param("type", type.toString())
                    .param("page", "0")
                    .param("limit", "10")
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()).andReturn();

            // then
            PagedResponse<ChallengeInfoDto> response = parseResponse(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {}
            );

            assertNotNull(response);
            assertThat(response.getData()).isNotNull();

            // count 타입 챌린지만 반환되었는지 검증
            if (!response.getData().isEmpty()) {
                response.getData().forEach(challenge -> {
                    assertThat(challenge.getCompletedType()).isEqualTo(CompletedType.count);
                    assertThat(challenge.getCurrentCount()).isNotNull(); // 현재 카운트가 있어야 함
                    assertThat(challenge.getConditionCount()).isNotNull(); // 목표 카운트가 있어야 함
                });
            }
        }

        @Test
        @DisplayName("200 OK - place 조건 챌린지 조회")
        void getMyChallengeList_placeType_isOk() throws Exception {
            // given
            String token = getTestUserToken1();
            CompletedType type = CompletedType.place;

            // when
            MvcResult result = mockMvc.perform(
                get(PREFIX)
                    .header(AUTH_HEADER, token)
                    .param("type", type.toString())
                    .param("page", "0")
                    .param("limit", "10")
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()).andReturn();

            // then
            PagedResponse<ChallengeInfoDto> response = parseResponse(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {}
            );

            assertNotNull(response);
            assertThat(response.getData()).isNotNull();

            // place 타입 챌린지만 반환되었는지 검증
            if (!response.getData().isEmpty()) {
                response.getData().forEach(challenge -> {
                    assertThat(challenge.getCompletedType()).isEqualTo(CompletedType.place);
                    assertThat(challenge.getConditionCount()).isNotNull(); // 목표 장소가 있어야 함
                });
            }
        }

        @Test
        @DisplayName("200 OK - 빈 결과 반환")
        void getMyChallengeList_emptyResult_isOk() throws Exception {
            // given
            String token = getTestUserToken1(); // 챌린지가 없는 사용자

            // when
            MvcResult result = mockMvc.perform(
                get(PREFIX)
                    .header(AUTH_HEADER, token)
                    .param("type", CompletedType.date.toString())
                    .param("page", "0")
                    .param("limit", "10")
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()).andReturn();

            // then
            PagedResponse<ChallengeInfoDto> response = parseResponse(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {}
            );

            assertNotNull(response);
            assertThat(response.getData()).isEmpty();
            assertThat(response.getTotalElements()).isEqualTo(0);
            assertThat(response.getTotalPages()).isEqualTo(0);
            assertThat(response.isFirst()).isTrue();
            assertThat(response.isLast()).isTrue();
        }
    }
}
