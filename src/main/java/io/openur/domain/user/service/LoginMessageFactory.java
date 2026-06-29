package io.openur.domain.user.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class LoginMessageFactory {

    public String create(String blockchainAddress, String nonce, LocalDateTime expiresAt) {
        return """
            OpenRun login

            Wallet: %s
            Nonce: %s
            Expires At: %s
            """.formatted(
            blockchainAddress,
            nonce,
            expiresAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        ).trim();
    }
}
