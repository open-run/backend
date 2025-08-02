package io.openur.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.openur.domain.user.model.Provider;
import io.openur.domain.user.model.User;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GetUserResponseDto {

    private String userId;
    private String nickname;
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
    private Integer feedback;

    public GetUserResponseDto(User user) {
        this.userId = user.getUserId();
        this.nickname = user.getNickname();
        this.identityAuthenticated = user.getIdentityAuthenticated();
        this.provider = user.getProvider();
        this.blackListed = user.getBlacklisted();
        this.createdDate = user.getCreatedDate();
        this.lastLoginDate = user.getLastLoginDate();
        this.blockchainAddress = user.getBlockchainAddress();
        this.runningPace = user.getRunningPace();
        this.runningFrequency = user.getRunningFrequency();
        this.feedback = user.getFeedback();
    }
}
