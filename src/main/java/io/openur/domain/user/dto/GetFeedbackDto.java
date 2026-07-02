package io.openur.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;

@Getter
public class GetFeedbackDto {

    @NotNull
    private List<String> targetUserIds;
    @NotBlank
    private String bungId;

}
