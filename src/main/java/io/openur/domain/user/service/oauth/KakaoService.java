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

@Slf4j(topic = "Kakao Login")
@Service
public class KakaoService extends LoginService {
    private final JwtUtil jwtUtil;

    // TODO: Properties Configuration으로 묶기
    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;
    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String tokenUri;
    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String userInfoUri;

    public KakaoService(
        UserRepositoryImpl userRepository,
        RestTemplate restTemplate,
        JwtUtil jwtUtil
    ) {
        super(userRepository, restTemplate);
		this.jwtUtil = jwtUtil;
	}

    // https://kauth.kakao.com/oauth/authorize?client_id={clientId}&redirect_uri=http://localhost:8080/v1/users/login/kakao&response_type=code
    public GetUsersLoginDto login(String code, String state) throws JsonProcessingException {
        // 1. "인가 코드"로 "액세스 토큰" 요청
        String accessToken = this.getAccessToken(code, clientId, redirectUri, tokenUri);

        // 2. 토큰으로 카카오 API 호출 : "액세스 토큰"으로 "카카오 사용자 정보" 가져오기
        OauthUserInfoDto kakaoUserInfo = getUserInfo(accessToken);

        // 3. 새로운 유저일 시 회원가입
        User kakaoUser = this.registerUserIfNew(kakaoUserInfo);

        // 4. JWT 토큰 생성 및 반환
        return new GetUsersLoginDto(
            kakaoUser.getEmail(),
            kakaoUser.getNickname(),
            jwtUtil.createToken(kakaoUser.getEmail())
        );
    }

    private OauthUserInfoDto getUserInfo(String accessToken) throws JsonProcessingException {
        ResponseEntity<String> response = this.getUserInfo(accessToken, userInfoUri);

        JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
        String email = jsonNode.get("kakao_account")
            .get("email").asText();

        log.info("카카오 사용자 정보: " + email);
        return new OauthUserInfoDto(email, Provider.kakao);
    }
}
