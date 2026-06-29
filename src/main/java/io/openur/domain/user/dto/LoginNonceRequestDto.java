package io.openur.domain.user.dto;

import io.openur.global.common.validation.ValidEthereumAddress;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginNonceRequestDto {

    @NotBlank
    @ValidEthereumAddress
    private String blockchainAddress;
}
