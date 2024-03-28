package io.openur.domain.user.service.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.openur.domain.user.dto.GetUsersLoginDto;
import io.openur.domain.user.dto.OauthUserInfoDto;
import io.openur.domain.user.model.Provider;
import io.openur.domain.user.model.User;
import io.openur.domain.user.repository.UserRepositoryImpl;
import io.openur.global.jwt.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j(topic = "Naver Login")
@Service
public class NaverService extends LoginService {
    private final JwtUtil jwtUtil;

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.naver.redirect-uri}")
    private String redirectUri;
    @Value("${spring.security.oauth2.client.provider.naver.token-uri}")
    private String tokenUri;
    @Value("${spring.security.oauth2.client.provider.naver.user-info-uri}")
    private String userInfoUri;
    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String clientSecret;

    public NaverService(UserRepositoryImpl userRepository,
        RestTemplate restTemplate, JwtUtil jwtUtil) {
        super(userRepository, restTemplate);
        this.jwtUtil = jwtUtil;
    }

    // https://nid.naver.com/oauth2.0/authorize?client_id={clientId}&response_type=code&redirect_uri=http://localhost:8080/v1/users/login/naver
    public GetUsersLoginDto login(String code, String state) throws JsonProcessingException {
        // 1. 접근 토큰 요청
        String accessToken = this.getAccessToken(code, clientId, redirectUri, tokenUri, clientSecret);

        // 2. 사용자 정보 요청
        OauthUserInfoDto naverUserInfo = getUserInfo(accessToken);

        // 3. 새로운 유저일 시 회원가입
        User naverUser = this.registerUserIfNew(naverUserInfo);

        // 4. JWT 토큰 반환
        return new GetUsersLoginDto(
            naverUser.getEmail(),
            naverUser.getNickname(),
            jwtUtil.createToken(naverUser.getEmail())
        );
    }

    private OauthUserInfoDto getUserInfo(String accessToken) throws JsonProcessingException {
        ResponseEntity<String> response = this.getUserInfo(accessToken, userInfoUri);

        JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
        String email = jsonNode.get("response").get("email").asText();

        log.info("네이버 사용자 정보: " + email);
        return new OauthUserInfoDto(email, Provider.naver);
    }
}
