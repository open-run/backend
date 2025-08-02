package io.openur.domain.user.service.login;

import io.openur.domain.user.model.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginServiceFactory {

    private final SmartWalletService smartWalletService;

    public LoginService getLoginService(Provider provider) {
        // TODO: login challenge publisher
        return switch (provider) {
            case smart_wallet -> smartWalletService;
            default -> throw new IllegalArgumentException("Invalid provider");
        };
    }
}
