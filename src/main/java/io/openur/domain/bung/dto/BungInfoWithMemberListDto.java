package io.openur.domain.bung.dto;

import io.openur.domain.bung.entity.BungEntity;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.userbung.dto.UserBungInfoDto;
import io.openur.domain.userbung.entity.UserBungEntity;
import io.openur.domain.userbung.model.UserBung;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BungInfoWithMemberListDto extends BungInfoDto {

    private List<UserBungInfoDto> memberList = new ArrayList<>();
    private Integer currentMemberCount = null;
    
    public BungInfoWithMemberListDto(
        BungEntity bungEntity, List<UserBungEntity> userBungEntities) {
        super(Bung.from(bungEntity));

        this.memberList =
            userBungEntities.stream()
                .map(UserBung::from)
                .map(UserBungInfoDto::new)
                .toList();
        this.currentMemberCount = memberList.size();
    }
    
    public BungInfoWithMemberListDto(Entry<BungEntity, UserBungEntity> entryMap) {
        // 인원수 카운트 공백 및 본인 혼자만 들어갈 예정
        super(Bung.from(entryMap.getKey()));

        this.memberList =
            List.of(new UserBungInfoDto(UserBung.from(entryMap.getValue())));
    }
    
    public BungInfoWithMemberListDto(BungEntity bung) {
        super(Bung.from(bung));
    }
}
