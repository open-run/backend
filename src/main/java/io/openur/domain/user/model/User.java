package io.openur.domain.user.model;


import io.openur.domain.user.dto.PatchUserSurveyRequestDto;
import io.openur.domain.user.entity.UserEntity;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

// QUESTION: What is the purpose of this class? Separate setter from UserEntity?
// Answer: Service에서 UserEntity 객체의 getter/setter를 직접적으로 사용하지 않기 위해
// Service와 Entity 사이의 모델 레이어를 추가함.
@AllArgsConstructor
public class User {
    private String userId;
    private Boolean withdraw;
    @Getter
    private String nickname;
    @Getter
    private String email;
    private Boolean identityAuthenticated;
    private Provider provider;
    private Boolean blacklisted;
    private LocalDateTime createdDate;
    private LocalDateTime lastLoginDate;
    private String blockchainAddress;
    private String learningFace;
    private Long learningFrequency;

    public User(
        String email,
        Provider provider
    ) {
        this.userId = UUID.randomUUID().toString();
        this.withdraw = false;
        this.nickname = null;
        this.email = email;
        this.identityAuthenticated = false;
        this.provider = provider;
        this.blacklisted = false;
        this.createdDate = LocalDateTime.now();
        this.lastLoginDate = LocalDateTime.now();
        this.blockchainAddress = "0x";
        this.learningFace = null;
        this.learningFrequency = null;
    }

    public static User from(final UserEntity userEntity){
        return new User(
            userEntity.getUserId(),
            userEntity.getWithdraw(),
            userEntity.getNickname(),
            userEntity.getEmail(),
            userEntity.getIdentityAuthenticated(),
            userEntity.getProvider(),
            userEntity.getBlacklisted(),
            userEntity.getCreatedDate(),
            userEntity.getLastLoginDate(),
            userEntity.getBlockchainAddress(),
            userEntity.getLearningFace(),
            userEntity.getLearningFrequency()
        );
    }

    public UserEntity toEntity(){
        return new UserEntity(
            userId,
            withdraw,
            nickname,
            email,
            identityAuthenticated,
            provider,
            blacklisted,
            createdDate,
            lastLoginDate,
            blockchainAddress,
            learningFace,
            learningFrequency

        );
    }


    public void update(PatchUserSurveyRequestDto patchUserSurveyRequestDto) {
        this.nickname = patchUserSurveyRequestDto.getNickname();
        this.learningFace = patchUserSurveyRequestDto.getLearningFace();
        this.learningFrequency = patchUserSurveyRequestDto.getLearningFrequency();
    }
}
