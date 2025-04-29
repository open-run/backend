package io.openur.domain.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.openur.domain.user.dto.GetFeedbackDto;
import io.openur.domain.user.dto.GetUserResponseDto;
import io.openur.domain.user.dto.GetUsersLoginDto;
import io.openur.domain.user.dto.GetUsersResponseDto;
import io.openur.domain.user.dto.PatchUserSurveyRequestDto;
import io.openur.domain.user.exception.UserNotFoundException;
import io.openur.domain.user.model.Provider;
import io.openur.domain.user.service.UserService;
import io.openur.domain.user.service.oauth.LoginService;
import io.openur.domain.user.service.oauth.LoginServiceFactory;
import io.openur.global.common.PagedResponse;
import io.openur.global.common.Response;
import io.openur.global.common.UtilController;
import io.openur.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
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
        LoginService loginService = loginServiceFactory.getLoginService(
            authServer);
        try {
            return ResponseEntity.ok()
                .body(Response.<GetUsersLoginDto>builder()
                    .message("success")
                    .data(loginService.login(code, state))
                    .build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping()
    @Operation(summary = "유저 정보 가져오기")
    public ResponseEntity<Response<GetUserResponseDto>> getUserInfo(
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        GetUserResponseDto getUserResponseDto = userService.getUserEmail(userDetails);
        return ResponseEntity.ok()
            .body(Response.<GetUserResponseDto>builder()
                .message("success")
                .data(getUserResponseDto)
                .build());
    }

    @GetMapping("/nickname")
    @Operation(summary = "닉네임으로 사용자 검색")
    public ResponseEntity<Response<List<GetUserResponseDto>>> findUsersInfo(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestParam String nickname
    ) {
        List<GetUserResponseDto> users = userService.searchByNickname(nickname);
        return ResponseEntity.ok()
            .body(Response.<List<GetUserResponseDto>>builder()
                .message("success")
                .data(users)
                .build());
    }

    @GetMapping("/nickname/exist")
    @Operation(summary = "닉네임 중복 체크")
    public ResponseEntity<Response<Boolean>> existNickname(
        @RequestParam(name = "nickname") 
        @NotBlank(message = "닉네임을 입력해주세요.") 
        @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하이여야 합니다.") 
        @Pattern(regexp = "^[가-힣a-zA-Z0-9]*$", message = "허용하지 않는 문자가 포함되어 있습니다.") String nickname
    ) {
        boolean isExist = userService.existNickname(nickname);
        return ResponseEntity.ok()
            .body(Response.<Boolean>builder()
                .message("success")
                .data(isExist)
                .build());
    }

    @GetMapping("/suggestion")
    @Operation(summary = "자주 함께한 사용자 목록")
    public ResponseEntity<PagedResponse<GetUsersResponseDto>> getUserSuggestion(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "10") int limit
    ) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<GetUsersResponseDto> users = userService.getUserSuggestion(userDetails, pageable);

        return ResponseEntity.ok().body(
                PagedResponse.build(users, "success"));
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
        return ResponseEntity.ok()
            .body(Response.<Void>builder()
                .message("success")
                .build());
    }

    @PatchMapping("/feedback")
    @Operation(summary = "벙 참가자들 피드백(좋아요) 증가")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "피드백이 성공적으로 증가됨", content = @Content(
            mediaType = "application/json", 
            examples = @ExampleObject(value = "{\"message\":\"Feedback increased successfully\"}")
        )),
        @ApiResponse(responseCode = "404", description = "일부 유저 ID를 찾을 수 없음", content = @Content(
            mediaType = "application/json", 
            examples = @ExampleObject(value = "{\"statusCode\":404,\"state\":\"NOT FOUND\",\"message\":\"Some user IDs were not found: [userId1, userId2]\"}")
        ))
    })
    public ResponseEntity<Response<List<String>>> increaseFeedback(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestBody @Valid GetFeedbackDto feedbackRequestDto) {
        List<String> notFoundUserIds = userService.increaseFeedback(userDetails,
            feedbackRequestDto.getBungId(),
            feedbackRequestDto.getTargetUserIds());

        if (!notFoundUserIds.isEmpty()) {
            throw new UserNotFoundException(notFoundUserIds);
        }

        Response<List<String>> response = Response.<List<String>>builder()
            .message("feedback increased successfully")
            .data(null)
            .build();
        return ResponseEntity.ok().body(response);
    }

    @PatchMapping("/wallet")
    @Operation(summary = "유저 지갑 주소 저장")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "지갑 주소가 성공적으로 저장됨", content = @Content(
            mediaType = "application/json", 
            examples = @ExampleObject(value = "{\"message\":\"Wallet address updated successfully\"}")
        )),
        @ApiResponse(responseCode = "400", description = "지갑 주소가 잘못된 형식임", content = @Content(
            mediaType = "application/json", 
            examples = @ExampleObject(value = "{\"statusCode\":400,\"state\":\"BAD REQUEST\",\"message\":\"Invalid wallet address\"}")
        )),
        @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음", content = @Content(
            mediaType = "application/json", 
            examples = @ExampleObject(value = "{\"statusCode\":404,\"state\":\"NOT FOUND\",\"message\":\"User not found\"}")
        )),
    })
    public ResponseEntity<Response<Void>> updateWalletAddress(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestParam String walletAddress  // TODO: 지갑 주소 형식 검증
    ) {
        userService.updateWalletAddress(userDetails, walletAddress);
        return ResponseEntity.ok()
            .body(Response.<Void>builder()
                .message("success")
                .build());
    }
}
