package io.openur.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.openur.domain.user.entity.UserEntity;
import io.openur.domain.user.model.Provider;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetUserResponseDto {
    private String userId;
    private final String nickname;
    private final String email;
    private Boolean identityAuthenticated;
    private Provider provider;
    private Boolean blackListed;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginDate;
    private String blockchainAddress;
    private String runningPace;
    private Integer runningFrequency;

    public GetUserResponseDto(UserEntity userEntity) {
        this.userId = userEntity.getUserId();
        this.nickname = userEntity.getNickname();
        this.email = userEntity.getEmail();
        this.identityAuthenticated = userEntity.getIdentityAuthenticated();
        this.provider = userEntity.getProvider();
        this.blackListed = userEntity.getBlacklisted();
        this.createdDate = userEntity.getCreatedDate();
        this.lastLoginDate = userEntity.getLastLoginDate();
        this.blockchainAddress = userEntity.getBlockchainAddress();
        this.runningPace = userEntity.getRunningPace();
        this.runningFrequency = userEntity.getRunningFrequency();
    }
}
