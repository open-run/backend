package io.openur.domain.bung.controller;

import io.openur.domain.bung.dto.GetBungDetailDto;
import io.openur.domain.bung.dto.PostBungEntityDto;
import io.openur.domain.bung.service.BungService;
import io.openur.global.common.Response;
import io.openur.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
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
    public ResponseEntity<Response> createBung(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestBody PostBungEntityDto requestDto
    ) {
        GetBungDetailDto createBung = bungService.createBungEntity(userDetails,
            requestDto);
        return ResponseEntity.ok().body(Response.<GetBungDetailDto>builder()
            .message("success")
            .data(createBung)
            .build());
    }

    @GetMapping()
    @Operation(summary = "벙 목록을 보는 경우 || 전체보기 || 참여한 ||")
    public ResponseEntity<Response> getBungList(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestParam(required = false, defaultValue = "false") boolean isOwned,
        @PageableDefault Pageable pageable
    ) {
        return null;
    } // TODO: users_bungs 가 생기면, users_bungs type 기준으로 필터 및 join 탐색 시행, boolean 교체

    @GetMapping("/{bungId}")
    @Operation(summary = "벙 정보 상세보기")
    public ResponseEntity<Response<GetBungDetailDto>> getBungDetail(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable String bungId
    ) {
        GetBungDetailDto getBung = bungService.getBungDetail(userDetails, bungId);
        return ResponseEntity.ok().body(Response.<GetBungDetailDto>builder()
            .message("success")
            .data(getBung)
            .build());
    }
}
