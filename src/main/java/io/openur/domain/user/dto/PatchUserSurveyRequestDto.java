package io.openur.domain.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class PatchUserSurveyRequestDto {

    @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하이여야 합니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]*$", message = "허용하지 않는 문자가 포함되어 있습니다.")
    private String nickname;

    @Pattern(regexp = "\\d{2}'\\d{2}\"", message = "형식에 맞지 않습니다.")
    private String runningPace;
    private Integer runningFrequency;
}
