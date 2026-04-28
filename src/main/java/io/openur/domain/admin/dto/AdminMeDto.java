package io.openur.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminMeDto {

    private boolean admin;

    public static AdminMeDto from(boolean admin) {
        return new AdminMeDto(admin);
    }
}
