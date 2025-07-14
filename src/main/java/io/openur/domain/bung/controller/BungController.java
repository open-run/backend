package io.openur.domain.bung.controller;

import io.openur.domain.bung.dto.BungInfoDto;
import io.openur.domain.bung.dto.BungInfoWithMemberListDto;
import io.openur.domain.bung.dto.BungInfoWithOwnershipDto;
import io.openur.domain.bung.dto.CreateBungDto;
import io.openur.domain.bung.dto.EditBungDto;
import io.openur.domain.bung.enums.CompleteBungResultEnum;
import io.openur.domain.bung.enums.EditBungResultEnum;
import io.openur.domain.bung.enums.JoinBungResultEnum;
import io.openur.domain.bung.enums.BungStatus;
import io.openur.domain.bung.service.BungService;
import io.openur.global.common.PagedResponse;
import io.openur.global.common.Response;
import io.openur.global.common.UtilController;
import io.openur.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/bungs")
@RequiredArgsConstructor
public class BungController {

    private final BungService bungService;

    @PostMapping()
    @Operation(summary = "벙 생성하기")
    public ResponseEntity<Response<Void>> createBung(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestBody CreateBungDto requestDto
    ) {
        BungInfoDto bung = bungService.createBung(userDetails, requestDto);
        return ResponseEntity.created(UtilController.createUri(bung.getBungId()))
            .body(Response.<Void>builder()
                .message("success")
                .build());
    }

    @GetMapping()
    @Operation(summary = "현재 시각 기준 벙 시작 시각이 미래인 벙 목록, 시작 시간 오름차순 (가장 임박한 벙 먼저)")
    public ResponseEntity<PagedResponse<BungInfoWithMemberListDto>> getBungList(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "10") int limit
    ) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<BungInfoWithMemberListDto> contents = bungService.getBungLists(
            userDetails, pageable
        );

        return ResponseEntity.ok().body(
            PagedResponse.build(contents, "success"));
    }

    @GetMapping("/location")
    @Operation(summary = "")
    public ResponseEntity<PagedResponse<BungInfoDto>> searchByLocation(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestParam String location,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "5") int limit
    ) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<BungInfoDto> contents = bungService.searchBungByLocation(
            userDetails, location, pageable
        );

        return ResponseEntity.ok().body(
            PagedResponse.build(contents, "success"));
    }

    @GetMapping("/nickname")
    @Operation(summary = "")
    public ResponseEntity<PagedResponse<BungInfoWithMemberListDto>> searchByNickname(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestParam String nickname,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "5") int limit
    ) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<BungInfoWithMemberListDto> contents = bungService.searchBungByNickname(
            userDetails, nickname, pageable
        );

        return ResponseEntity.ok().body(
            PagedResponse.build(contents, "success")
        );
    }

    @GetMapping("/hashtag")
    @Operation(summary = "")
    public ResponseEntity<PagedResponse<BungInfoDto>> searchByHashtag(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestParam(required = true, defaultValue = "") List<String> hashtag,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "5") int limit
    ) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<BungInfoDto> contents = bungService.searchBungByHashtag(
            userDetails, hashtag, pageable
        );

        return ResponseEntity.ok().body(
            PagedResponse.build(contents, "success")
        );
    }

    @GetMapping("/my-bungs")
    @Operation(summary = """
        내가 소유 및 참가했던 벙 목록.
        status == PARTICIPATING, ONGOING, PENDING 이면 시작 시간 오름차순 (가장 임박한 벙 먼저),
        status == (ACCOMPLISHED or null) 이면 시작 시간 내림차순 (가장 최근에 완료된 or 가장 먼 미래의 벙 먼저)
        """
    )
    public ResponseEntity<PagedResponse<BungInfoWithOwnershipDto>> getMyBungList(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @Parameter(description = "null : 전부 || true : 소유한 || false : 소유자는 아닌")
        @RequestParam(required = false, defaultValue = "") Boolean isOwned,
        @Parameter(description = "null : 전부 || PARTICIPATING : 아직 시작하지 않은 || ACCOMPLISHED : 완료된")
        @RequestParam(required = false, defaultValue = "ALL") BungStatus status,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "10") int limit
    ) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<BungInfoWithOwnershipDto> contents = bungService.getMyBungLists(
            userDetails, isOwned, status, pageable);

        return ResponseEntity.ok().body(
            PagedResponse.build(contents, "success"));
    }

    @GetMapping("/{bungId}")
    @Operation(summary = "벙 정보 상세보기 (unauthenticated. 유저 여부와 상관 없이 벙 정보 조회 가능)")
    public ResponseEntity<Response<BungInfoWithMemberListDto>> getBungDetail(
        @PathVariable String bungId
    ) {
        BungInfoWithMemberListDto getBung = bungService.getBungDetail(bungId);
        return ResponseEntity.ok().body(Response.<BungInfoWithMemberListDto>builder()
            .message("success")
            .data(getBung)
            .build());
    }

    @DeleteMapping("/{bungId}")
    @Operation(summary = "벙 삭제하기")
    public ResponseEntity<Response<Void>> deleteBung(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable String bungId
    ) {
        bungService.deleteBung(userDetails, bungId);
        return ResponseEntity.ok().body(Response.<Void>builder()
            .message("success")
            .build());
    }

    @GetMapping("/{bungId}/join")
    @Operation(summary = "벙 참가하기")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "성공적으로 참가됨", content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = "{\"message\":\"successfully joined\",\"data\":\"SUCCESSFULLY_JOINED\"}")
        )),
        @ApiResponse(responseCode = "409", description = "특정 사유로 참가가 반려됨", content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = "{\"statusCode\":409,\"state\":\"CONFLICT\",\"message\":\"bung has already started\"}")
        ))
    })
    public ResponseEntity<Response<JoinBungResultEnum>> joinBung(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable String bungId
    ) {
        JoinBungResultEnum result = bungService.joinBung(userDetails, bungId);
        return ResponseEntity.ok().body(Response.<JoinBungResultEnum>builder()
            .message(result.toString())
            .data(result)
            .build());
    }

    @PatchMapping("/{bungId}")
    @Operation(summary = "벙 수정하기")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "성공적으로 수정됨", content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = "{\"message\":\"successfully edited\",\"data\":\"SUCCESSFULLY_EDITED\"}")
        )),
        @ApiResponse(responseCode = "403", description = "특정 사유로 수정이 금지됨", content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = "{\"statusCode\":403, \"state\":\"FORBIDDEN\", \"message\":\"You cannot edit bung - bung has already completed\"}")
        ))
    })
    public ResponseEntity<Response<EditBungResultEnum>> editBung(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable String bungId,
        @RequestBody @Valid EditBungDto editBungDto
    ) {
        EditBungResultEnum result = bungService.editBung(userDetails, bungId, editBungDto);
        return ResponseEntity.ok().body(Response.<EditBungResultEnum>builder()
            .message(result.toString())
            .data(result)
            .build());
    }

    @PatchMapping("/{bungId}/complete")
    @Operation(summary = "벙 완료하기")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "성공적으로 완료됨", content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = "{\"message\":\"success\"}")
        )),
        @ApiResponse(responseCode = "409", description = "특정 사유로 완료가 반려됨", content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = "{\"statusCode\":409, \"state\":\"CONFLICT\", \"message\":\"bung is already completed\"}")
        )),
        @ApiResponse(responseCode = "409", description = "특정 사유로 완료가 반려됨", content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = "{\"statusCode\":409, \"state\":\"CONFLICT\", \"message\":\"bung has not started yet\"}")
        ))
    })
    public ResponseEntity<Response<CompleteBungResultEnum>> completeBung(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable String bungId
    ) {
        CompleteBungResultEnum result = bungService.completeBung(userDetails, bungId);
        return ResponseEntity.ok().body(Response.<CompleteBungResultEnum>builder()
            .message(result.toString())
            .data(result)
            .build());
    }
}

