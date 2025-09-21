package io.openur.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import io.openur.config.TestSupport;
import io.openur.domain.challenge.dto.GeneralChallengeDto;
import io.openur.global.common.PagedResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ChallengeApiTest extends TestSupport {

    private static final String PREFIX = "/v1/challenges";

    @Nested
    @DisplayName("내 챌린지 목록 조회 - 조건별 테스트")
    class getMyChallengeListTest {

        @Test
        @DisplayName("200 OK - 일반 챌린지 빈 결과")
        void getGeneralChallengeList_emptyResult_isOk() throws Exception {
            // given
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
            assertThat(response.getData()).isEmpty();
            assertThat(response.getTotalElements()).isEqualTo(0);
            assertThat(response.getTotalPages()).isEqualTo(0);
            assertThat(response.isFirst()).isTrue();
            assertThat(response.isLast()).isTrue();
        }

        @Test
        @DisplayName("200 OK - 완료된 챌린지 빈 결과")
        void getCompletedChallengeList_emptyResult_isOk() throws Exception {
            // given
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
            assertThat(response.getData()).isEmpty();
            assertThat(response.getTotalElements()).isEqualTo(0);
            assertThat(response.getMessage()).isEqualTo("success");
        }
    }

}
