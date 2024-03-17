package io.openur.domain.user.dto;

import io.openur.domain.user.model.Provider;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OauthUserInfoDto {
    private String email;
    private Provider provider;

    public OauthUserInfoDto(
        String email,
        Provider provider
    ) {
        this.email = email;
        this.provider = provider;
    }
}
