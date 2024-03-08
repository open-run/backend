package io.openur.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@AllArgsConstructor
public class GetUserDto {
    private Long userId;
    private Boolean withdraw;
    private final String nickname;
    private final String email;
    private Boolean identityAuthenticated;
    private String provider;
    private Boolean blackListed;
    private Timestamp createdDate;
    private Timestamp lastLoginDate;
    private String blockchainAddress;
    private Boolean isNewAccount = false;
}
