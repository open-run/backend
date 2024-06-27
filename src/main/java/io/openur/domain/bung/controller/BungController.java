package io.openur.domain.bung.controller;

import io.openur.domain.bung.dto.BungDetailDto;
import io.openur.domain.bung.dto.PostBungEntityDto;
import io.openur.domain.bung.service.BungService;
import io.openur.global.common.Response;
import io.openur.global.common.UtilController;
import io.openur.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
        BungDetailDto bung = bungService.createBungEntity(userDetails, requestDto);
        return ResponseEntity.created(UtilController.createUri(bung.getBungId()))
            .body(Response.<Void>builder()
            .message("success")
            .build());
    }

    @GetMapping()
    @Operation(summary = "벙 목록을 보는 경우 || 전체보기 || 참여한 ||")
    public ResponseEntity<Response> getBungList(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @Parameter(description = "참여한 벙 목록만 보는 경우 true 로 설정")
        @RequestParam(required = false, defaultValue = "false") boolean isParticipating,
        @PageableDefault Pageable pageable
    ) {
        return null;
    } // TODO: users_bungs 가 생기면, users_bungs type 기준으로 필터 및 join 탐색 시행, boolean 교체

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
}
