package io.openur.domain.bung.dto.Req;

import java.util.List;
import lombok.Getter;

@Getter
public class InviteMembersRequestDto {
    private List<String> userIds;
}
