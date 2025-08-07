package io.openur.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetNonceResponseDto {
    private final String nonce;
}
