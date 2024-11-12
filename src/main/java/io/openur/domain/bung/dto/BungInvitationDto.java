package io.openur.domain.bung.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class BungInvitationDto {
    private Long bungId;
    private List<Long> userIdList;

}
