package io.openur.domain.userbung.controller;

import io.openur.domain.userbung.service.UserBungService;
import io.openur.global.common.Response;
import io.openur.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
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
	@Operation(summary = "멤버 삭제하기(벙주만 가능)")
	public ResponseEntity<Response<Void>> kickMember(
		@AuthenticationPrincipal UserDetailsImpl userDetails,
		@PathVariable String bungId,
		@PathVariable String userIdToRemove
	) {
		userBungService.removeUserFromBung(userDetails, bungId, userIdToRemove);
		// TODO: Delete endpoint들의 response status accepted -> ok로 변경하기
		return ResponseEntity.accepted().body(Response.<Void>builder()
			.message("success")
			.build());
	}
}
