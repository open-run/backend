package io.openur.domain.bung.dto;

import io.openur.domain.userbung.model.UserBung;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BungInfoWithOwnershipDto extends BungInfoDto {

    private boolean hasOwnership;

    public BungInfoWithOwnershipDto(UserBung userBung) {
        super(userBung.getBung());
        this.hasOwnership = userBung.isOwner();
    }
}
