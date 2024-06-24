package io.openur.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.openur.config.TestSupport;
import java.time.LocalDateTime;
import java.util.HashMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

public class BungControllerTest extends TestSupport {
    private static final String PREFIX = "/v1/bungs";

    @Test
    @DisplayName("Bung : 벙 생성 테스트")
    void createBungTest() throws Exception {
        String token = getTestUserToken();

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
        ).andExpect(status().isOk());
    }
}
