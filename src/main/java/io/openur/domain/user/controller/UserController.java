package io.openur.domain.user.controller;

import io.openur.domain.user.dto.ExistNicknameRequestDto;
import io.openur.domain.user.dto.GetUserResponseDto;
import io.openur.domain.user.dto.PostUserDto;
import io.openur.domain.user.service.UserService;
import io.openur.global.common.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/v1/user")
    @Operation(summary = "유저 로그인 성공 시 정보 반환")
    public ResponseEntity<Response<PostUserDto>> postUser(){
        // TODO: load authorized user info from UserService
        return ResponseEntity.ok()
            .body(Response.<PostUserDto>builder()
                .message("success")
//                .data(new PostUserDto("test"))
                .build());
    }

    @GetMapping("/v1/users")
    @Operation(summary = "유저 정보 가져오기")
    public ResponseEntity<Response<GetUserResponseDto>> getUserInfo(@PathVariable Long userId) {
        GetUserResponseDto getUserResponseDto = userService.getUserById(userId);
        return ResponseEntity.ok().body(Response.<GetUserResponseDto>builder()
            .message("success")
            .data(getUserResponseDto)
            .build());
    }

    @GetMapping("/v1/users/nickname/exist")
    @Operation(summary = "닉네임 중복 체크")
    public ResponseEntity<Response<Boolean>> existNickname(@RequestBody @Valid ExistNicknameRequestDto existNicknameRequestDto) {
        boolean existNickname = userService.existNickname(
            existNicknameRequestDto.getNickname());
        return ResponseEntity.ok().body(Response.<Boolean>builder()
            .message("success")
            .data(existNickname)
            .build());
    }


}
