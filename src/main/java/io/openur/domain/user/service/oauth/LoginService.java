package io.openur.domain.user.service.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.openur.domain.user.dto.GetUsersLoginDto;
import io.openur.domain.user.dto.OauthUserInfoDto;
import io.openur.domain.user.model.User;
import io.openur.domain.user.repository.UserRepositoryImpl;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
public abstract class LoginService {
    private final UserRepositoryImpl userRepository;
    private final RestTemplate restTemplate;

    public abstract GetUsersLoginDto login(String code, String state) throws JsonProcessingException;

    protected String getAccessToken(String code, String clientId, String redirectUri, String tokenUri)
        throws JsonProcessingException {
        return this.getAccessToken(code, clientId, redirectUri, tokenUri, null);
    }

    protected String getAccessToken(
        String code,
        String clientId,
        String redirectUri,
        String tokenUri,
        String clientSecret
        ) throws JsonProcessingException {
        URI uri = UriComponentsBuilder.fromUriString(tokenUri).encode().build().toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
            .post(uri)
            .headers(headers)
            .body(body);

        ResponseEntity<String> response = restTemplate.exchange(
            requestEntity,
            String.class
        );

        JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
        return jsonNode.get("access_token").asText();
    }

    protected User registerUserIfNew(OauthUserInfoDto oauthUserInfoDto) {
        // DB 에 중복된 이메일의 유저가 있는지 확인
        String email = oauthUserInfoDto.getEmail();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            // 없으면 회원가입
            User newUser = new User(email, oauthUserInfoDto.getProvider());
            return userRepository.save(newUser);
        }
        else {
            return user;
        }
    }

    protected ResponseEntity<String> getUserInfo(
        String accessToken,
        String userInfoUri
    ) {
        // 요청 URL 만들기
        URI uri = UriComponentsBuilder.fromUriString(userInfoUri).encode().build().toUri();

        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        RequestEntity<Void> requestEntity = RequestEntity
            .get(uri)
            .headers(headers)
            .build();

        // HTTP 요청 보내기
        return restTemplate.exchange(
            requestEntity,
            String.class
        );
    }

}
