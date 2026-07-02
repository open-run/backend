package io.openur.global.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.openur.config.TestSupport;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtUtilTest extends TestSupport {

    @Test
    @DisplayName("access token 만료 시간은 7일이다")
    void accessTokenExpiresInSevenDays() throws Exception {
        String token = jwtUtil.createToken("0x1234567890123456789012345678901234567890")
            .substring(JwtUtil.BEARER_PREFIX.length());

        JsonNode payload = decodePayload(token);

        assertThat(payload.get("exp").asLong() - payload.get("iat").asLong())
            .isEqualTo(Duration.ofDays(7).toSeconds());
    }

    private JsonNode decodePayload(String token) throws Exception {
        String payload = token.split("\\.")[1];
        byte[] decodedPayload = Base64.getUrlDecoder().decode(payload);
        return new ObjectMapper().readTree(new String(decodedPayload, StandardCharsets.UTF_8));
    }
}
