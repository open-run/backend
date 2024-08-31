package io.openur.domain.bung.controller;

import io.openur.domain.bung.dto.BungDetailDto;
import io.openur.domain.bung.dto.PostBungEntityDto;
import io.openur.domain.bung.dto.Req.InviteMembersRequestDto;
import io.openur.domain.bung.service.BungService;
import io.openur.global.common.PagedResponse;
import io.openur.global.common.Response;
import io.openur.global.common.UtilController;
import io.openur.global.enums.BungStatus;
import io.openur.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
    @Operation(summary = "벙을 생성하는 경우")
    public ResponseEntity<Response<Void>> createBung(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestBody PostBungEntityDto requestDto
    ) {
        BungDetailDto bung = bungService.createBung(userDetails, requestDto);
        return ResponseEntity.created(UtilController.createUri(bung.getBungId()))
            .body(Response.<Void>builder()
            .message("success")
            .build());
    }

    @GetMapping()
    @Operation(summary = "벙 목록 || 전체보기 || 합류한 || 출석한 ")
    public ResponseEntity<PagedResponse<BungDetailDto>> getBungList(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @Parameter(description = "참여한 벙 목록만 보는 경우 true 로 설정")
        @RequestParam(required = false, defaultValue = "ALL") BungStatus status,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "10") int limit
    ) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<BungDetailDto> contents = bungService.getBungLists(userDetails, status, pageable);

        return ResponseEntity.ok().body(PagedResponse.build(
            "success", contents)
        );
    }

    @GetMapping("/my-bungs")
    @Operation(summary = "내가 소유한 벙 ID 목록과 벙 정보 가져오기")
    public ResponseEntity<PagedResponse<BungDetailDto>> getOwnedBungDetails(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "10") int limit
    ) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<BungDetailDto> ownedBungDetails = bungService.getOwnedBungLists(userDetails, pageable);

        return ResponseEntity.ok().body(PagedResponse.build("success", ownedBungDetails));
    }

    @GetMapping("/{bungId}")
    @Operation(summary = "벙 정보 상세보기")
    public ResponseEntity<Response<BungDetailDto>> getBungDetail(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable String bungId
    ) {
        // TODO: 벙 참가자도 같이 받고 싶을지도?
        BungDetailDto getBung = bungService.getBungDetail(userDetails, bungId);
        return ResponseEntity.ok().body(Response.<BungDetailDto>builder()
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
        return ResponseEntity.accepted().body(Response.<Void>builder()
            .message("success")
            .build());
    }

    @PostMapping("/{bungId}/invitation")
    @Operation(summary = "멤버 초대하기 ")
    public ResponseEntity<Response<Void>> inviteMembers(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable String bungId,
        @RequestBody InviteMembersRequestDto req
    ) {
        return ResponseEntity.created(UtilController.createUri(bungId))
            .body(Response.<Void>builder()
            .message("Successfully invited")
            .build());
    }
}
