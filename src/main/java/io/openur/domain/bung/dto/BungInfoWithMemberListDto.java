package io.openur.domain.bung.dto;

import io.openur.domain.bung.entity.BungEntity;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.userbung.dto.UserBungInfoDto;
import io.openur.domain.userbung.entity.UserBungEntity;
import io.openur.domain.userbung.model.UserBung;
import java.util.List;
import java.util.Map.Entry;
import lombok.Getter;

@Getter
public class BungInfoWithMemberListDto extends BungInfoDto {
    private List<UserBungInfoDto> memberList;

    public BungInfoWithMemberListDto(
        Entry<BungEntity, List<UserBungEntity>> entrySet) {
        super(Bung.from(entrySet.getKey()));

        this.memberList =
            entrySet.getValue()
                .stream()
                .map(userBungEntity -> new UserBungInfoDto(UserBung.from(userBungEntity)))
                .toList();
    }
}
