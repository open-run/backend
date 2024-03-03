package io.openur.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostUserDto {
    private final Boolean isNewAccount = false;
    private final String email;
    private final String nickname;
}
