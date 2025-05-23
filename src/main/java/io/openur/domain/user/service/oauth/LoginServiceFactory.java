package io.openur.domain.user.service.oauth;

import io.openur.domain.user.model.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginServiceFactory {

    private final KakaoService kakaoService;
    private final NaverService naverService;
    private final SmartWalletService smartWalletService;

    public LoginService getLoginService(Provider provider) {
        // TODO: login challenge publisher
        return switch (provider) {
            case kakao -> kakaoService;
            case naver -> naverService;
            case smart_wallet -> smartWalletService;
            default -> throw new IllegalArgumentException("Invalid provider");
        };
    }
}
