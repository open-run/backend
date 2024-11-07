package io.openur.domain.bung.controller;

import io.openur.domain.bung.dto.BungInfoDto;
import io.openur.domain.bung.dto.BungInfoWithMemberListDto;
import io.openur.domain.bung.dto.CreateBungDto;
import io.openur.domain.bung.model.BungStatus;
import io.openur.domain.bung.service.BungService;
import io.openur.global.common.PagedResponse;
import io.openur.global.common.Response;
import io.openur.global.common.UtilController;
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
        @RequestBody CreateBungDto requestDto
    ) {
        BungInfoDto bung = bungService.createBung(userDetails, requestDto);
        return ResponseEntity.created(UtilController.createUri(bung.getBungId()))
            .body(Response.<Void>builder()
            .message("success")
            .build());
    }

    @GetMapping()
    @Operation(summary = "현재 시각 기준 시작 시각이 미래인 벙 목록")
    public ResponseEntity<PagedResponse<BungInfoDto>> getBungList(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @Parameter(description = "true : 참가하지 않은 || false : 참가 유무와 상관 없이 전체 벙 목록")
        @RequestParam(required = false, defaultValue = "false") boolean isAvailableOnly,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "10") int limit
    ) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<BungInfoDto> contents = bungService.getBungLists(userDetails, isAvailableOnly, pageable);

        return ResponseEntity.ok().body(
            PagedResponse.build(contents, "success"));
    }

    @GetMapping("/my-bungs")
    @Operation(summary = "내가 소유 및 참가했던 벙 목록")
    public ResponseEntity<PagedResponse<BungInfoDto>> getMyBungList(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @Parameter(description = "null : 전부 || true : 소유한 || false : 소유자는 아닌")
        @RequestParam(required = false, defaultValue = "") Boolean isOwned,
        @Parameter(description = "null : 전부 || PARTICIPATING : 아직 시작하지 않은 || ACCOMPLISHED : 완료된")
        @RequestParam(required = false, defaultValue = "") BungStatus status,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "10") int limit
    ) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<BungInfoDto> contents = bungService.getMyBungLists(
            userDetails, isOwned, status, pageable);

        return ResponseEntity.ok().body(
            PagedResponse.build(contents, "success"));
    }

    @GetMapping("/{bungId}")
    @Operation(summary = "벙 정보 상세보기")
    public ResponseEntity<Response<BungInfoWithMemberListDto>> getBungDetail(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable String bungId
    ) {
        BungInfoWithMemberListDto getBung = bungService.getBungDetail(userDetails, bungId);
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
        return ResponseEntity.accepted().body(Response.<Void>builder()
            .message("success")
            .build());
    }
}
