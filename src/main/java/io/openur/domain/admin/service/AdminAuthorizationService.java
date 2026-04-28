package io.openur.domain.admin.service;

import io.openur.global.security.UserDetailsImpl;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AdminAuthorizationService {

    private final Set<String> adminWalletAddresses;

    public AdminAuthorizationService(
        @Value("${openrun.admin.wallet-addresses:}") String adminWalletAddresses
    ) {
        this.adminWalletAddresses = Arrays.stream(adminWalletAddresses.split(","))
            .map(String::trim)
            .filter(StringUtils::hasText)
            .map(address -> address.toLowerCase(Locale.ROOT))
            .collect(Collectors.toUnmodifiableSet());
    }

    public boolean isAdmin(UserDetailsImpl userDetails) {
        if (userDetails == null || userDetails.getUser() == null) {
            return false;
        }

        String walletAddress = userDetails.getUser().getBlockchainAddress();
        return StringUtils.hasText(walletAddress)
            && adminWalletAddresses.contains(walletAddress.toLowerCase(Locale.ROOT));
    }

    public void assertAdmin(UserDetailsImpl userDetails) {
        if (!isAdmin(userDetails)) {
            throw new AccessDeniedException("Admin permission is required");
        }
    }
}
