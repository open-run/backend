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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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

import java.util.List;

@RestController
@RequestMapping("/v1/bungs")
@RequiredArgsConstructor
@EnableMethodSecurity
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

    @DeleteMapping("/{bungId}")
    @Operation(summary = "벙 삭제하기")
    @PreAuthorize("@bungService.isOwnerOfBung(#userDetails, #bungId)")
    public ResponseEntity<Response<Void>> deleteBung(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable String bungId
    ) {
        bungService.deleteBung(bungId);
        return ResponseEntity.accepted().body(Response.<Void>builder()
            .message("success")
            .build());
    }

    // TODO: move @PreAuthorize to Service class
    @PatchMapping("/{bungId}/change-owner")
    @Operation(summary = "벙주 변경(벙주만 가능)")
    @PreAuthorize("@bungService.isOwnerOfBung(#userDetails, #bungId)")
    public ResponseEntity<Response<Void>> changeOwner(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable String bungId,
        @RequestParam String newOwnerUserId
    ) {
        bungService.changeOwner(bungId, newOwnerUserId);
        return ResponseEntity.ok().body(Response.<Void>builder()
            .message("Owner changed successfully")
            .build());
    }

    @DeleteMapping("/{bungId}/members/{userIdToRemove}")
    @Operation(summary = "멤버 삭제하기(벙주만 가능)")
    @PreAuthorize("@bungService.isOwnerOfBung(#userDetails, #bungId)")
    public ResponseEntity<Response<Void>> kickMember(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable String bungId,
        @PathVariable String userIdToRemove
    ) {
        bungService.removeUserFromBung(bungId, userIdToRemove);
        // TODO: Delete endpoint들의 response status accepted -> ok로 변경하기
        return ResponseEntity.accepted().body(Response.<Void>builder()
            .message("success")
            .build());
    }


    @GetMapping("/my-bungs")
    @Operation(summary = "내가 소유한 벙 ID 목록과 벙 정보 가져오기")
    public ResponseEntity<Response<List<BungDetailDto>>> getOwnedBungDetails(
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        List<BungDetailDto> ownedBungDetails = bungService.getOwnedBungDetails(userDetails);
        return ResponseEntity.ok().body(Response.<List<BungDetailDto>>builder()
            .message("success")
            .data(ownedBungDetails)
            .build());
    }


}
