package io.openur.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetUsersLoginDto {
    private final String email;
    private final String nickname;
    private final String jwtToken;
}
