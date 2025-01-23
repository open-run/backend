package io.openur.domain.user.model;

import static io.openur.global.common.UtilController.applyIfNotNull;

import io.openur.domain.user.dto.PatchUserSurveyRequestDto;
import io.openur.domain.user.entity.UserEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

// QUESTION: What is the purpose of this class? Separate setter from UserEntity?
// Answer: Service에서 UserEntity 객체의 getter/setter를 직접적으로 사용하지 않기 위해
// Service와 Entity 사이의 모델 레이어를 추가함.
@Getter
@AllArgsConstructor
public class User {

    private String userId;
    private String nickname;
    private String email;
    private Boolean identityAuthenticated;
    private Provider provider;
    private Boolean blacklisted;
    private LocalDateTime createdDate;
    private LocalDateTime lastLoginDate;
    private String blockchainAddress;
    private String runningPace;
    private Integer runningFrequency;
    private Integer feedback;

    public User(
        String email,
        Provider provider
    ) {
        this.userId = UUID.randomUUID().toString();
        this.nickname = null;
        this.email = email;
        this.identityAuthenticated = false;
        this.provider = provider;
        this.blacklisted = false;
        this.createdDate = LocalDateTime.now();
        this.lastLoginDate = LocalDateTime.now();
        this.blockchainAddress = "0x";
        this.runningPace = null;
        this.runningFrequency = null;
        this.feedback = 0;
    }

    public static User from(final UserEntity userEntity) {
        return new User(
            userEntity.getUserId(),
            userEntity.getNickname(),
            userEntity.getEmail(),
            userEntity.getIdentityAuthenticated(),
            userEntity.getProvider(),
            userEntity.getBlacklisted(),
            userEntity.getCreatedDate(),
            userEntity.getLastLoginDate(),
            userEntity.getBlockchainAddress(),
            userEntity.getRunningPace(),
            userEntity.getRunningFrequency(),
            userEntity.getFeedback()
        );
    }

    public UserEntity toEntity() {
        return new UserEntity(
            userId,
            nickname,
            email,
            identityAuthenticated,
            provider,
            blacklisted,
            createdDate,
            lastLoginDate,
            blockchainAddress,
            runningPace,
            runningFrequency,
            feedback

        );
    }


    public void update(PatchUserSurveyRequestDto patchUserSurveyRequestDto) {
        applyIfNotNull(patchUserSurveyRequestDto.getNickname(), newNickname -> this.nickname = newNickname);
        applyIfNotNull(patchUserSurveyRequestDto.getRunningPace(), newRunningPace -> this.runningPace = newRunningPace);
        applyIfNotNull(patchUserSurveyRequestDto.getRunningFrequency(),
            newRunningFrequency -> this.runningFrequency = newRunningFrequency);
    }
}
