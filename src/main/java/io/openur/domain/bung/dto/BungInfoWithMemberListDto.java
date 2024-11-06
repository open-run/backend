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
public class BungInfoWithMemberListDto extends BungInfoWithHashtagsDto {
    private List<UserBungInfoDto> memberList;

    public BungInfoWithMemberListDto(
		Entry<BungEntity, List<UserBungEntity>> entrySet,
		List<String> hashtags
	) {
		super(Bung.from(entrySet.getKey()), hashtags);

        this.memberList =
            entrySet.getValue()
                .stream()
                .map(userBungEntity -> new UserBungInfoDto(UserBung.from(userBungEntity)))
                .toList();
    }
}
