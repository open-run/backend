package io.openur.domain.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.openur.domain.user.dto.ExistNicknameRequestDto;
import io.openur.domain.user.dto.GetUserResponseDto;
import io.openur.domain.user.dto.GetUsersLoginDto;
import io.openur.domain.user.model.Provider;
import io.openur.domain.user.service.UserService;
import io.openur.domain.user.service.oauth.LoginService;
import io.openur.domain.user.service.oauth.LoginServiceFactory;
import io.openur.global.common.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class UserController {
    private final LoginServiceFactory loginServiceFactory;
    private final UserService userService;

    @GetMapping("/v1/users/login/{authServer}")
    @Operation(summary = "유저 로그인 성공 시 정보 반환")
    public ResponseEntity<Response<GetUsersLoginDto>> getUser(
        @PathVariable Provider authServer,
        @RequestParam String code,
        @RequestParam(required = false) String state
    ){
        // TODO: different method using Spring Security
        //  oAuth2AuthorizedClientService.loadAuthorizedClient(authServer.name().toLowerCase(), "test");

        LoginService loginService = loginServiceFactory.getLoginService(authServer);
		try {
			return ResponseEntity.ok()
				.body(Response.<GetUsersLoginDto>builder()
					.message("success")
					.data(loginService.login(code, state))
					.build()
				);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
    }

    @GetMapping("/v1/users/{userId}")
    @Operation(summary = "유저 정보 가져오기")
    public ResponseEntity<Response<GetUserResponseDto>> getUserInfo(
		// TODO: change to jwtToken as RequestHeader
		@PathVariable Long userId
	) {
        GetUserResponseDto getUserResponseDto = userService.getUserById(userId);
        return ResponseEntity.ok().body(Response.<GetUserResponseDto>builder()
            .message("success")
            .data(getUserResponseDto)
            .build());
    }

    @GetMapping("/v1/users/nickname/exist")
    @Operation(summary = "닉네임 중복 체크")
    public ResponseEntity<Response<Boolean>> existNickname(
		@RequestBody @Valid ExistNicknameRequestDto existNicknameRequestDto
	) {
        boolean existNickname = userService.existNickname(
            existNicknameRequestDto.getNickname());
        return ResponseEntity.ok().body(Response.<Boolean>builder()
            .message("success")
            .data(existNickname)
            .build());
    }


}
