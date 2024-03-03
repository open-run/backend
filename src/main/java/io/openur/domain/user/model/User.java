package io.openur.domain.user.model;


import io.openur.domain.user.entity.UserEntity;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;

// QUESTION: What is the purpose of this class? Separate setter from UserEntity?
@AllArgsConstructor
public class User {
    private Long userId;
    private Boolean withdraw;
    private String nickname;
    private String email;
    private Boolean identityAuthenticated;
    private Provider provider;
    private Boolean blackListed;
    private Timestamp createdDate;
    private Timestamp lastLoginDate;
    private String blockchainAddress;

    public User(String email) {
        this.email = email;
    }


    public static User from(final UserEntity userEntity){
        return new User(
            userEntity.getUserId(),
            userEntity.getWithdraw(),
            userEntity.getNickname(),
            userEntity.getEmail(),
            userEntity.getIdentityAuthenticated(),
            Provider.valueOf(userEntity.getProvider()),
            userEntity.getBlackListed(),
            userEntity.getCreatedDate(),
            userEntity.getLastLoginDate(),
            userEntity.getBlockchainAddress()
        );
    }

    public UserEntity toEntity(){
        return new UserEntity(
            userId,
            withdraw,
            nickname,
            email,
            identityAuthenticated,
            provider.name(),
            blackListed,
            createdDate,
            lastLoginDate,
            blockchainAddress
        );
    }


}
