package io.openur.domain.user.service.oauth;

import io.openur.domain.user.model.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginServiceFactory {
    private final KakaoService kakaoService;
    private final NaverService naverService;

    public LoginService getLoginService(Provider provider) {
		return switch (provider) {
			case kakao -> kakaoService;
			case naver -> naverService;
			default -> throw new IllegalArgumentException("Invalid provider");
		};
    }
}
