package io.openur.domain.user.dto;

import io.openur.domain.user.entity.UserEntity;
import io.openur.domain.user.model.Provider;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@AllArgsConstructor
public class GetUserResponseDto {
    private Long userId;
    private Boolean withdraw;
    private final String nickname;
    private final String email;
    private Boolean identityAuthenticated;
    private Provider provider;
    private Boolean blackListed;
    private LocalDateTime createdDate;
    private LocalDateTime lastLoginDate;
    private String blockchainAddress;

    public GetUserResponseDto(UserEntity userEntity) {
        this.userId = userEntity.getUserId();
            this.withdraw = userEntity.getWithdraw();
            this.nickname = userEntity.getNickname();
            this.email = userEntity.getEmail();
            this.identityAuthenticated = userEntity.getIdentityAuthenticated();
            this.provider = userEntity.getProvider();
            this.blackListed = userEntity.getBlackListed();
            this.createdDate = userEntity.getCreatedDate();
            this.lastLoginDate = userEntity.getLastLoginDate();
            this.blockchainAddress = userEntity.getBlockchainAddress();
    }
}
