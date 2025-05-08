package io.openur.domain.bung.dto;

import io.openur.domain.bung.entity.BungEntity;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.userbung.dto.UserBungInfoDto;
import io.openur.domain.userbung.entity.UserBungEntity;
import io.openur.domain.userbung.model.UserBung;
import java.util.List;
import java.util.Map.Entry;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BungInfoWithMemberListDto extends BungInfoDto {

    private List<UserBungInfoDto> memberList;
    
    public BungInfoWithMemberListDto(
        Entry<BungEntity, List<UserBungEntity>> entry) {
        super(Bung.from(entry.getKey()));

        this.memberList =
            entry.getValue().stream()
                .map(userBungEntity -> new UserBungInfoDto(UserBung.from(userBungEntity)))
                .toList();
    }

    public BungInfoWithMemberListDto(
        BungEntity bungEntity, List<UserBungEntity> userBungEntities) {
        super(Bung.from(bungEntity));

        this.memberList =
            userBungEntities.stream()
                .map(userBungEntity -> new UserBungInfoDto(UserBung.from(userBungEntity)))
                .toList();
    }
}
