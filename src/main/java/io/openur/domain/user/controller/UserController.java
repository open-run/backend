package io.openur.domain.user.controller;

import io.openur.domain.user.dto.PostUserDto;
import io.openur.domain.user.entity.UserEntity;
import io.openur.domain.user.service.UserService;
import io.openur.global.common.Response;
import io.swagger.v3.oas.annotations.Operation;
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

    @GetMapping("/v1/user")
    @Operation(summary = "유저 정보 가져오기")
    public ResponseEntity<PostUserDto> getUserInfo(@PathVariable Long userId) {
        UserEntity userEntity = userService.getUserById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        PostUserDto postUserDto = new PostUserDto(
            userEntity.getIsNewAccount(),
            userEntity.getEmail(),
            userEntity.getNickname()
        );

        return ResponseEntity.ok().body(postUserDto);
    }


}
