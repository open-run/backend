package io.openur.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetUsersLoginDto {
    @Deprecated
    private final String email;       // TODO: 삭제 예정
    private final String identifier;  // Can be email or blockchain address
    private final String nickname;
    private final String jwtToken;
}
