package io.openur.domain.user.dto;

import io.openur.domain.user.model.Provider;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SmartWalletUserInfoDto {
    private final String blockchainAddress;
    private final Provider provider;

    public static SmartWalletUserInfoDto of(String blockchainAddress) {
        return new SmartWalletUserInfoDto(blockchainAddress, Provider.smart_wallet);
    }
} 