package io.openur.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.openur.domain.user.model.Provider;
import io.openur.domain.user.model.User;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetSelfResponseDto {
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

    public GetSelfResponseDto(User user) {
        this.userId = user.getUserId();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.identityAuthenticated = user.getIdentityAuthenticated();
        this.provider = user.getProvider();
        this.blackListed = user.getBlacklisted();
        this.createdDate = user.getCreatedDate();
        this.lastLoginDate = user.getLastLoginDate();
        this.blockchainAddress = user.getBlockchainAddress();
        this.runningPace = user.getRunningPace();
        this.runningFrequency = user.getRunningFrequency();
    }
}
