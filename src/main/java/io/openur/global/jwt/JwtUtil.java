package io.openur.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j(topic = "JwtUtil")
@Component
public class JwtUtil {

    // Header KEY 값
    public static final String AUTHORIZATION_HEADER = "Authorization";
    // Token 식별자
    public static final String BEARER_PREFIX = "Bearer ";
    // 토큰 만료시간 TODO : 개발 프로세스 상 너무 짧아 임시 6시간으로 연장합니다, 추후 더 늘릴지, 정책 논의 필요합니다!
    private final long TOKEN_TIME = 6 * 60 * 60 * 1000L; // 60분
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
    @Value("${jwt.secret.key}") // Base64 Encode 한 SecretKey
    private String secretKey;
    private Key key;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    public String createToken(String blockchainAddress) {
        Date date = new Date();

        return BEARER_PREFIX +
            Jwts.builder()
                .setSubject(blockchainAddress) // 사용자 식별자값(ID)
                .setExpiration(new Date(date.getTime() + TOKEN_TIME)) // 만료 시간
                .setIssuedAt(date) // 발급일
                .signWith(key, signatureAlgorithm) // 암호화 알고리즘
                .compact();
    }

    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private Jws<Claims> validateToken(String token) throws InvalidJwtException {
        String msg;
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
        } catch (SecurityException | MalformedJwtException e) {
            msg = "Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.";
        } catch (ExpiredJwtException e) {
            msg = "Expired JWT token, 만료된 JWT token 입니다.";
        } catch (UnsupportedJwtException e) {
            msg = "Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.";
        }
        log.error(msg);
        throw new InvalidJwtException(msg);
    }

    public Claims getUserInfoFromToken(String token) {
        Jws<Claims> jws = this.validateToken(token);
        return jws.getBody();
    }
}
