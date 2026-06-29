package io.openur.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginNonceResponseDto {
    private final String nonce;
    private final String message;
}
