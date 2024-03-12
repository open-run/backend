package io.openur.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ExistNicknameRequestDto {
    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하이여야 합니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]*$", message = "허용하지 않는 문자가 포함되어 있습니다.")
    private String nickname;
}
