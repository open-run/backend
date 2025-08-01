package io.openur.domain.bung.dto;

import io.openur.domain.bung.entity.BungEntity;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.userbung.dto.UserBungInfoDto;
import io.openur.domain.userbung.entity.UserBungEntity;
import io.openur.domain.userbung.model.UserBung;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BungInfoWithMemberListDto extends BungInfoDto {

    private List<UserBungInfoDto> memberList = new ArrayList<>();
    
    public BungInfoWithMemberListDto(
        BungEntity bungEntity, List<UserBungEntity> userBungEntities) {
        super(Bung.from(bungEntity));

        this.memberList = userBungEntities.stream()
            .map(UserBung::from)
            .map(UserBungInfoDto::new)
            .toList();
    }
}
