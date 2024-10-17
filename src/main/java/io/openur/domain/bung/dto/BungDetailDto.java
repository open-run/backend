package io.openur.domain.bung.dto;

import io.openur.domain.bung.model.Bung;
import io.openur.domain.userbung.dto.UserBungInfoDto;
import io.openur.domain.userbung.model.UserBung;
import java.util.List;
import lombok.Getter;

@Getter
public class BungDetailDto extends BungInfoDto {
    private List<UserBungInfoDto> memberList;

    public BungDetailDto(Bung bung, List<UserBung> members) {
        super(bung);

        this.memberList = members.stream().map(UserBungInfoDto::new).toList();
    }
}
