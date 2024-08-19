package io.openur.domain.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.openur.domain.user.dto.GetSelfResponseDto;
import io.openur.domain.user.dto.GetUsersLoginDto;
import io.openur.domain.user.dto.GetUsersResponseDto;
import io.openur.domain.user.dto.PatchUserSurveyRequestDto;
import io.openur.domain.user.model.Provider;
import io.openur.domain.user.service.UserService;
import io.openur.domain.user.service.oauth.LoginService;
import io.openur.domain.user.service.oauth.LoginServiceFactory;
import io.openur.global.common.Response;
import io.openur.global.common.UtilController;
import io.openur.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final LoginServiceFactory loginServiceFactory;
    private final UserService userService;

    @GetMapping("/login/{authServer}")
    @Operation(summary = "유저 로그인 성공 시 정보 반환")
    public ResponseEntity<Response<GetUsersLoginDto>> getUser(
        @PathVariable Provider authServer,
        @RequestParam String code,
        @RequestParam(required = false) String state
    ) {
        // TODO: different method using Spring Security
        //  oAuth2AuthorizedClientService.loadAuthorizedClient(authServer.name().toLowerCase(), "test");

        LoginService loginService = loginServiceFactory.getLoginService(
            authServer);
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

    @GetMapping()
    @Operation(summary = "유저 정보 가져오기")
    public ResponseEntity<Response<GetSelfResponseDto>> getUserInfo(
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        GetSelfResponseDto getSelfResponseDto = userService.getUserEmail(
            userDetails);
        return ResponseEntity.ok().body(Response.<GetSelfResponseDto>builder()
            .message("success")
            .data(getSelfResponseDto)
            .build());
    }

    @GetMapping("/suggestion")
    @Operation(summary = "자주 같이 띈 참여자 리스트")
    public ResponseEntity<Response<Page<GetUsersResponseDto>>> getNeighbors(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "10") int limit
    ) {
        Pageable pageable = PageRequest.of(page, limit);

        return ResponseEntity.ok().body(Response.<Page<GetUsersResponseDto>>builder()
            .message("success")
            .data(userService.getMemberSuggestion(userDetails, pageable))
            .build());
    }

    @GetMapping("/nickname/exist")
    @Operation(summary = "닉네임 중복 체크")
    public ResponseEntity<Response<Boolean>> existNickname(
        @RequestParam(name = "nickname")
        @NotBlank(message = "닉네임을 입력해주세요.")
        @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하이여야 합니다.")
        @Pattern(regexp = "^[가-힣a-zA-Z0-9]*$", message = "허용하지 않는 문자가 포함되어 있습니다.")
        String nickname
    ) {
        boolean isExist = userService.existNickname(nickname);
        return ResponseEntity.ok().body(Response.<Boolean>builder()
            .message("success")
            .data(isExist)
            .build());
    }

    @PatchMapping()
    @Operation(summary = "설문조사 결과 저장")
    public ResponseEntity<Response<Void>> saveSurveyResult(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestBody @Valid PatchUserSurveyRequestDto patchUserSurveyRequestDto
    ) {
        userService.saveSurveyResult(userDetails, patchUserSurveyRequestDto);

        String userId = userService.getUserById(userDetails);

        return ResponseEntity.created(UtilController.createUri(userId))
            .body(Response.<Void>builder()
                .message("success")
                .build());
    }

    @DeleteMapping()
    @Operation(summary = "유저 정보 삭제(탈퇴)")
    public ResponseEntity<Response<Void>> deleteUserInfo(
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        userService.deleteUserInfo(userDetails);
        return ResponseEntity.accepted()
            .body(Response.<Void>builder().message("success").build());
    }
}
