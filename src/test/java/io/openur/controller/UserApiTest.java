package io.openur.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.openur.config.TestSupport;
import io.openur.domain.user.dto.GetUserResponseDto;
import io.openur.domain.user.dto.LoginNonceResponseDto;
import io.openur.global.dto.Response;
import jakarta.servlet.http.Cookie;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

public class UserApiTest extends TestSupport {

    private static final String PREFIX = "/v1/users";
    private static final String REFRESH_TOKEN_COOKIE = "OPENRUN_REFRESH_TOKEN";
    private static final String ALLOWED_ORIGIN = "https://open-run.vercel.app";

    @Test
    @DisplayName("닉네임 중복확인")
    void getExistNicknameTest() throws Exception {
        String testName = "zXc9U1i01";

        mockMvc.perform(
                get(PREFIX + "/nickname/exist?nickname={nickname}", testName)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("유저 정보")
    void getUserInfoTest() throws Exception {
        String token = getTestUserToken1();

        mockMvc.perform(
                get(PREFIX)
                    .header(AUTH_HEADER, token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.profileImageUrl").value(org.hamcrest.Matchers.nullValue()));
    }

    @Nested
    @DisplayName("세션 갱신")
    class RefreshSessionTest {

        @Test
        @DisplayName("로그인 성공 시 HttpOnly refresh token cookie를 내려준다")
        void loginSetsRefreshTokenCookie() throws Exception {
            MvcResult result = smartWalletLogin(BigInteger.valueOf(1));

            String setCookie = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
            assertThat(setCookie)
                .contains(REFRESH_TOKEN_COOKIE + "=")
                .contains("HttpOnly")
                .contains("Path=/v1/auth")
                .contains("SameSite=None");
        }

        @Test
        @DisplayName("refresh token으로 access token을 재발급하고 refresh token을 회전한다")
        void refreshRotatesRefreshToken() throws Exception {
            MvcResult loginResult = smartWalletLogin(BigInteger.valueOf(2));
            String oldRefreshToken = extractRefreshToken(loginResult);

            MvcResult refreshResult = mockMvc.perform(
                    post("/v1/auth/refresh")
                        .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                        .cookie(new Cookie(REFRESH_TOKEN_COOKIE, oldRefreshToken))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.jwtToken").exists())
                .andReturn();

            String newRefreshToken = extractRefreshToken(refreshResult);
            assertThat(newRefreshToken).isNotEqualTo(oldRefreshToken);

            mockMvc.perform(
                    post("/v1/auth/refresh")
                        .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                        .cookie(new Cookie(REFRESH_TOKEN_COOKIE, oldRefreshToken))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("logout은 refresh token을 폐기하고 cookie를 제거한다")
        void logoutRevokesRefreshToken() throws Exception {
            MvcResult loginResult = smartWalletLogin(BigInteger.valueOf(3));
            String refreshToken = extractRefreshToken(loginResult);

            MvcResult logoutResult = mockMvc.perform(
                    post("/v1/auth/logout")
                        .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                        .cookie(new Cookie(REFRESH_TOKEN_COOKIE, refreshToken))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

            assertThat(logoutResult.getResponse().getHeader(HttpHeaders.SET_COOKIE))
                .contains(REFRESH_TOKEN_COOKIE + "=")
                .contains("Max-Age=0")
                .contains("Path=/v1/auth");

            mockMvc.perform(
                    post("/v1/auth/refresh")
                        .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                        .cookie(new Cookie(REFRESH_TOKEN_COOKIE, refreshToken))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("logout은 같은 지갑의 회전된 refresh token도 폐기한다")
        void logoutRevokesRotatedRefreshTokenForSameWallet() throws Exception {
            MvcResult loginResult = smartWalletLogin(BigInteger.valueOf(4));
            String oldRefreshToken = extractRefreshToken(loginResult);

            MvcResult refreshResult = mockMvc.perform(
                    post("/v1/auth/refresh")
                        .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                        .cookie(new Cookie(REFRESH_TOKEN_COOKIE, oldRefreshToken))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
            String newRefreshToken = extractRefreshToken(refreshResult);

            mockMvc.perform(
                    post("/v1/auth/logout")
                        .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                        .cookie(new Cookie(REFRESH_TOKEN_COOKIE, oldRefreshToken))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());

            mockMvc.perform(
                    post("/v1/auth/refresh")
                        .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                        .cookie(new Cookie(REFRESH_TOKEN_COOKIE, newRefreshToken))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("cookie 기반 auth endpoint는 Origin이 없으면 거부한다")
        void cookieAuthEndpointRejectsMissingOrigin() throws Exception {
            mockMvc.perform(
                    post("/v1/auth/refresh")
                        .cookie(new Cookie(REFRESH_TOKEN_COOKIE, "dummy"))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());
        }

        private MvcResult smartWalletLogin(BigInteger privateKey) throws Exception {
            ECKeyPair keyPair = ECKeyPair.create(privateKey);
            String walletAddress = "0x" + Keys.getAddress(keyPair.getPublicKey());
            LoginNonceResponseDto loginNonce = issueLoginNonce(walletAddress);
            String signature = signMessage(loginNonce.getMessage(), keyPair);

            var request = new HashMap<>();
            request.put("code", walletAddress);
            request.put("nonce", loginNonce.getNonce());
            request.put("state", signature);

            return mockMvc.perform(
                    post(PREFIX + "/login/smart_wallet")
                        .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonify(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.jwtToken").exists())
                .andReturn();
        }

        private LoginNonceResponseDto issueLoginNonce(String walletAddress) throws Exception {
            var request = new HashMap<>();
            request.put("blockchainAddress", walletAddress);

            MvcResult result = mockMvc.perform(
                    post("/v1/auth/login-nonce")
                        .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonify(request))
                )
                .andExpect(status().isOk())
                .andReturn();

            JsonNode data = new ObjectMapper()
                .readTree(result.getResponse().getContentAsString())
                .get("data");
            return new LoginNonceResponseDto(
                data.get("nonce").asText(),
                data.get("message").asText()
            );
        }

        private String signMessage(String message, ECKeyPair keyPair) {
            Sign.SignatureData signatureData = Sign.signPrefixedMessage(
                message.getBytes(StandardCharsets.UTF_8),
                keyPair
            );
            byte[] signature = new byte[65];
            System.arraycopy(signatureData.getR(), 0, signature, 0, 32);
            System.arraycopy(signatureData.getS(), 0, signature, 32, 32);
            System.arraycopy(signatureData.getV(), 0, signature, 64, 1);
            return Numeric.toHexString(signature);
        }

        private String extractRefreshToken(MvcResult result) {
            String setCookie = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
            assertThat(setCookie).isNotBlank();

            String prefix = REFRESH_TOKEN_COOKIE + "=";
            assertThat(setCookie).startsWith(prefix);

            int endIndex = setCookie.indexOf(';');
            assertThat(endIndex).isGreaterThan(prefix.length());
            return setCookie.substring(prefix.length(), endIndex);
        }
    }

    @Test
    @DisplayName("프로필 요약")
    void getProfileSummaryTest() throws Exception {
        when(nftMintClient.mintToken(any(String.class), any(BigInteger.class), any(BigInteger.class)))
            .thenReturn("0xtxhash");

        mockMvc.perform(
            post("/v1/nft/mint-jobs")
                .header(AUTH_HEADER, getTestUserToken1())
                .param("userChallengeId", "1")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

        mockMvc.perform(
            post("/v1/nft/mint-jobs")
                .header(AUTH_HEADER, getTestUserToken1())
                .param("userChallengeId", "2")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

        var feedbackRequest = new HashMap<>();
        feedbackRequest.put("bungId", "c0477004-1632-455f-acc9-04584b55921f");
        feedbackRequest.put("targetUserIds", List.of("9e1bfc60-f76a-47dc-9147-803653707192"));

        mockMvc.perform(
            patch(PREFIX + "/feedback")
                .header(AUTH_HEADER, getTestUserToken2())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonify(feedbackRequest))
        ).andExpect(status().isOk());

        mockMvc.perform(
            get(PREFIX + "/profile-summary")
                .header(AUTH_HEADER, getTestUserToken1())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("success"))
            .andExpect(jsonPath("$.data.receivedLikeCount").value(1))
            .andExpect(jsonPath("$.data.currentOwnedBungCount").value(1))
            .andExpect(jsonPath("$.data.acquiredNftCount").value(2))
            .andExpect(jsonPath("$.data.recentAcquiredNfts", hasSize(2)))
            .andExpect(jsonPath("$.data.recentAcquiredNfts[0].challengeName").exists())
            .andExpect(jsonPath("$.data.recentAcquiredNfts[0].acquiredAt").exists())
            .andExpect(jsonPath("$.data.recentAcquiredNfts[0].nft.transactionHash").value("0xtxhash"));
    }

    @Nested
    @DisplayName("유저 닉네임으로 검색")
    class getUserByNicknameTest {

        @Test
        @DisplayName("200 Ok.")
        void getUserByNickname_isOk() throws Exception {
            String token = getTestUserToken1();

            MvcResult result = mockMvc.perform(
                    get(PREFIX + "/nickname?nickname={nickname}", "test")
                        .header(AUTH_HEADER, token)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk()).andReturn();

            Response<List<GetUserResponseDto>> response = parseResponse(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
            );
            assert response.getData().size() == 3;
        }

        @Test
        @DisplayName("400 Bad Request. Query parameter is required.")
        void getUserByNickname_isBadRequest() throws Exception {
            String token = getTestUserToken1();

            mockMvc.perform(
                    get(PREFIX + "/nickname")
                        .header(AUTH_HEADER, token)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("페이스 기록 입력 저장")
    class saveSurveyResultTest {

        @Test
        @DisplayName("400 Bad Request. 잘못된 형식의 인풋")
        void saveSurveyResult_isBadRequest() throws Exception {
            String token = getTestUserToken1();
            var surveyResult = new HashMap<>();
            surveyResult.put("nickname", "나도모른거");
            surveyResult.put("runningPace", "5'55\"");  // 형식에 맞지 않음. 5'55" -> 05'55"
            surveyResult.put("runningFrequency", "2");
            mockMvc.perform(
                    patch(PREFIX)
                        .header(AUTH_HEADER, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonify(surveyResult))
                )
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("201 Created.")
        @Transactional
        void saveSurveyResult_isOk() throws Exception {
            String token = getTestUserToken1();
            var surveyResult = new HashMap<>();
            surveyResult.put("nickname", "나도모른거");
            surveyResult.put("runningPace", "05'55\"");
            surveyResult.put("runningFrequency", 2);
            mockMvc.perform(
                    patch(PREFIX)
                        .header(AUTH_HEADER, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonify(surveyResult))
                )
                .andExpect(status().isCreated());
        }
    }
}
