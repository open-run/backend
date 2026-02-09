package io.openur.controller;


import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import io.openur.config.TestSupport;
import io.openur.global.dto.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class HashtagApiTest extends TestSupport {

    private static final String PREFIX = "/v1/bungs";

    @Test
    @DisplayName("기존 해시태그 조회")
    void getHashtagsTest() throws Exception {
        String token = getTestUserToken1();
        String uriPath = PREFIX + "/hashtags?tag=";
        Map<String, List<String>> requestAndResponse = Map.of(
            "런", Arrays.asList("펀런", "런린이"),
            "고수", List.of("고수만"),
            "급벙", new ArrayList<>()
        );

        for (Entry<String, List<String>> entry : requestAndResponse.entrySet()) {
            MvcResult result = mockMvc.perform(
                get(uriPath + entry.getKey())
                    .header(AUTH_HEADER, token)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()).andReturn();

            Response<List<String>> response = parseResponse(
                result.getResponse().getContentAsString(), new TypeReference<>() {
                });
            assert response.getData().containsAll(entry.getValue());
        }
    }
}
