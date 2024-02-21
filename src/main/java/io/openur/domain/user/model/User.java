package io.openur.domain.user.model;


import io.openur.domain.user.entity.UserEntity;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class User {
    // user entity 그대로 다 가져오기
    private Long userId;
    private Boolean withdraw;
    private String nickname;

//    public static User from(final UserEntity userEntity){
//        return new User(
//            userEntity.getUserId(),
//            userEntity.getWithdraw(),
//            userEntity.getNickname(),
//            ...이어서작성
//        );
//    }

//    public UserEntity toEntity(){
//        return new UserEntity(
//            userId,
//            withdraw,
//            nickname,
//            ...이어서
//        );
//    }


}
