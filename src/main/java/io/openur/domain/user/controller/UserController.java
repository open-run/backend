package io.openur.domain.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.openur.domain.user.dto.ExistNicknameRequestDto;
import io.openur.domain.user.dto.GetUserResponseDto;
import io.openur.domain.user.dto.GetUsersLoginDto;
import io.openur.domain.user.dto.PatchUserSurveyRequestDto;
import io.openur.domain.user.model.Provider;
import io.openur.domain.user.service.UserService;
import io.openur.domain.user.service.oauth.LoginService;
import io.openur.domain.user.service.oauth.LoginServiceFactory;
import io.openur.global.common.Response;
import io.openur.global.jwt.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


@RestController
@RequiredArgsConstructor
public class UserController {
    private final LoginServiceFactory loginServiceFactory;
    private final UserService userService;
    private final JwtUtil jwtUtil;

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
        HttpServletRequest request
	) {
        String jwtToken = jwtUtil.getJwtFromHeader(request);
        if (Objects.requireNonNull(jwtToken) == null) {
            // JWT 토큰이 존재하지 않는 경우 처리
            return ResponseEntity.ok().body(Response.<GetUserResponseDto>builder()
                .message("jwtToken is not available")
                .build());
        }else{
            GetUserResponseDto getUserResponseDto = userService.getUserEmail(jwtToken);
            return ResponseEntity.ok().body(Response.<GetUserResponseDto>builder()
                .message("success")
                .data(getUserResponseDto)
                .build());
        }
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

    @PatchMapping("/v1/users/{userId}")
    @Operation(summary = "설문조사 결과 저장")
    public ResponseEntity<Response<Void>> saveSurveyResult(
        HttpServletRequest request,
        @RequestBody @Valid PatchUserSurveyRequestDto patchUserSurveyRequestDto
    ) {
        String jwtToken = jwtUtil.getJwtFromHeader(request);
        if (Objects.requireNonNull(jwtToken) == null) {
            // JWT 토큰이 존재하지 않는 경우 처리
            return ResponseEntity.ok()
                .body(Response.<Void>builder()
                    .message("jwtToken is not available")
                    .build());
        }else{
            userService.saveSurveyResult(jwtToken, patchUserSurveyRequestDto);

            String userId = userService.getUserById(jwtToken);

            return ResponseEntity.created(createUri(userId))
                .body(Response.<Void>builder()
                    .message("success")
                    .build());
        }
    }

    private URI createUri(String todoId) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(todoId)
            .toUri();
    }

}
