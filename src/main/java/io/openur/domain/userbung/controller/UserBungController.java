package io.openur.domain.userbung.controller;

import io.openur.domain.userbung.service.UserBungService;
import io.openur.global.common.Response;
import io.openur.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/bungs")
@RequiredArgsConstructor
@Tag(name = "user-bung-controller", description = "특정 벙의 멤버와 관련된 액션들")
public class UserBungController {

    private final UserBungService userBungService;

    @PatchMapping("/{bungId}/change-owner")
    @Operation(summary = "벙주 변경(벙주만 가능)")
    public ResponseEntity<Response<Void>> changeOwner(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable String bungId,
        @RequestParam String newOwnerUserId
    ) {
        userBungService.changeOwner(userDetails, bungId, newOwnerUserId);
        return ResponseEntity.ok().body(Response.<Void>builder()
            .message("Owner changed successfully")
            .build());
    }

    @DeleteMapping("/{bungId}/members/{userIdToRemove}")
    @Operation(summary = "멤버 삭제하기(벙주와 본인만 가능)")
    public ResponseEntity<Response<Void>> kickMember(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable String bungId,
        @PathVariable String userIdToRemove
    ) {
        userBungService.removeUserFromBung(userDetails, bungId, userIdToRemove);
        return ResponseEntity.ok().body(Response.<Void>builder()
            .message("Member deleted successfully")
            .build());
    }

    @PatchMapping("/{bungId}/participated")
    @Operation(summary = "벙 참여 인증 완료(벙 참여 중인 본인만 가능)")
    public ResponseEntity<Response<Void>> confirmBungParticipation(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable String bungId
    ) {
        userBungService.confirmBungParticipation(userDetails, bungId);
        return ResponseEntity.ok().body(Response.<Void>builder()
            .message("Bung completion verified successfully")
            .build());
    }
}
