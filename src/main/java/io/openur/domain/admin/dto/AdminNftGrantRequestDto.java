package io.openur.domain.admin.dto;

import io.openur.global.common.validation.ValidEthereumAddress;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminNftGrantRequestDto {

    @ValidEthereumAddress
    private String recipientAddress;

    @NotNull(message = "nftItemId is required")
    private Long nftItemId;
}
