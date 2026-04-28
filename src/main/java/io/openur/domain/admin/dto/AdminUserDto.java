package io.openur.domain.admin.dto;

import io.openur.domain.user.entity.UserEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminUserDto {

    private String userId;
    private String nickname;
    private String blockchainAddress;

    public static AdminUserDto from(UserEntity user) {
        return AdminUserDto.builder()
            .userId(user.getUserId())
            .nickname(user.getNickname())
            .blockchainAddress(user.getBlockchainAddress())
            .build();
    }
}
