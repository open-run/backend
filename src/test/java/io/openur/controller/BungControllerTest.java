package io.openur.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.openur.config.TestSupport;
import io.openur.global.common.Response;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public class BungControllerTest extends TestSupport {
    private static final String PREFIX = "/v1/bungs";

    @Test
    @DisplayName("Bung : 벙 생성 테스트")
    void createBungTest() throws Exception {
        String token = getBungOwnerToken();

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

        MvcResult result = mockMvc.perform(
            post(PREFIX)
                .header(AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonify(submittedBung)))
            .andExpect(status().isCreated())
            .andReturn();

        String returnedUri = Objects.requireNonNull(result.getResponse().getHeaderValue("Location"))
            .toString();
        Response<String> response = new ObjectMapper().readValue(
            result.getResponse().getContentAsString(),
            Response.class);

        String uri = ServletUriComponentsBuilder.fromCurrentRequest()
            .path(PREFIX + "/{bungId}")
            .buildAndExpand(response.getData())
            .toUriString();
        assert returnedUri.equals(uri);
    }

    @Nested
    class deleteBungTest {

        String bungId = "c0477004-1632-455f-acc9-04584b55921f";

        @Test
        @DisplayName("Bung: 벙 삭제 401 실패")
        @Transactional
        void deleteBung_isUnauthorized() throws Exception {
            // TODO: authentication 에러 확인
            //  Request processing failed: java.lang.NullPointerException: Cannot invoke "io.openur.global.security.UserDetailsImpl.getUser()" because "userDetails" is null
            mockMvc.perform(
                    delete(PREFIX + "/" + bungId)
                )
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Bung: 벙 삭제 202 성공")
        @Transactional
        void deleteBung_isAccepted() throws Exception {
            String token = getBungOwnerToken();
            mockMvc.perform(
                    delete(PREFIX + "/" + bungId)
                        .header(AUTH_HEADER, token))
                .andExpect(status().isAccepted());
        }

    }
}
