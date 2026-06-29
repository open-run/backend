package io.openur.domain.user.service;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCookieFactory {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "OPENRUN_REFRESH_TOKEN";
    private static final String REFRESH_TOKEN_COOKIE_PATH = "/v1/auth";

    @Value("${openrun.auth.refresh-token.max-age-seconds:2592000}")
    private long refreshTokenMaxAgeSeconds;

    @Value("${openrun.auth.refresh-token.cookie-secure:true}")
    private boolean cookieSecure;

    @Value("${openrun.auth.refresh-token.cookie-same-site:None}")
    private String cookieSameSite;

    public ResponseCookie create(String refreshToken) {
        return baseCookie(refreshToken)
            .maxAge(Duration.ofSeconds(refreshTokenMaxAgeSeconds))
            .build();
    }

    public ResponseCookie clear() {
        return baseCookie("")
            .maxAge(Duration.ZERO)
            .build();
    }

    private ResponseCookie.ResponseCookieBuilder baseCookie(String value) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, value)
            .httpOnly(true)
            .secure(cookieSecure)
            .sameSite(cookieSameSite)
            .path(REFRESH_TOKEN_COOKIE_PATH);
    }
}
