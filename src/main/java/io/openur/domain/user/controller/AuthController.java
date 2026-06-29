package io.openur.domain.user.controller;

import static io.openur.domain.user.service.RefreshTokenCookieFactory.REFRESH_TOKEN_COOKIE_NAME;

import io.openur.domain.user.dto.LoginNonceRequestDto;
import io.openur.domain.user.dto.LoginNonceResponseDto;
import io.openur.domain.user.dto.RefreshTokenResponseDto;
import io.openur.domain.user.service.LoginNonceService;
import io.openur.domain.user.service.RefreshTokenCookieFactory;
import io.openur.domain.user.service.RefreshTokenService;
import io.openur.global.dto.Response;
import io.openur.global.jwt.InvalidJwtException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenCookieFactory refreshTokenCookieFactory;
    private final LoginNonceService loginNonceService;

    @PostMapping("/login-nonce")
    public ResponseEntity<Response<LoginNonceResponseDto>> issueLoginNonce(
        @RequestBody @Valid LoginNonceRequestDto request
    ) {
        return ResponseEntity.ok()
            .body(Response.<LoginNonceResponseDto>builder()
                .message("success")
                .data(loginNonceService.issue(request.getBlockchainAddress()))
                .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<Response<RefreshTokenResponseDto>> refresh(
        @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken
    ) {
        if (!StringUtils.hasText(refreshToken)) {
            throw new InvalidJwtException("Missing refresh token");
        }

        RefreshTokenService.SessionTokens sessionTokens = refreshTokenService.rotate(refreshToken);
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, refreshTokenCookieFactory.create(sessionTokens.refreshToken()).toString())
            .body(Response.<RefreshTokenResponseDto>builder()
                .message("success")
                .data(new RefreshTokenResponseDto(sessionTokens.jwtToken()))
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<Response<Void>> logout(
        @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken
    ) {
        if (StringUtils.hasText(refreshToken)) {
            refreshTokenService.revoke(refreshToken);
        }

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, refreshTokenCookieFactory.clear().toString())
            .body(Response.<Void>builder()
                .message("success")
                .build());
    }
}
