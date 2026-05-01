package io.openur.domain.admin.controller;

import io.openur.domain.NFT.dto.NftAvatarItemDto;
import io.openur.domain.admin.dto.AdminChallengeDto;
import io.openur.domain.admin.dto.AdminChallengeRequestDto;
import io.openur.domain.admin.dto.AdminMeDto;
import io.openur.domain.admin.dto.AdminNftGrantRequestDto;
import io.openur.domain.admin.dto.AdminNftGrantResponseDto;
import io.openur.domain.admin.dto.AdminNftItemDto;
import io.openur.domain.admin.dto.AdminUserDto;
import io.openur.domain.admin.service.AdminAuthorizationService;
import io.openur.domain.admin.service.AdminChallengeService;
import io.openur.domain.admin.service.AdminNftService;
import io.openur.domain.admin.service.AdminUserService;
import io.openur.global.dto.Response;
import io.openur.global.security.UserDetailsImpl;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminAuthorizationService adminAuthorizationService;
    private final AdminChallengeService adminChallengeService;
    private final AdminNftService adminNftService;
    private final AdminUserService adminUserService;

    @GetMapping("/me")
    public ResponseEntity<Response<AdminMeDto>> getAdminMe(
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        boolean admin = adminAuthorizationService.isAdmin(userDetails);

        return ResponseEntity.ok(Response.<AdminMeDto>builder()
            .data(AdminMeDto.from(admin))
            .message("Admin status fetched successfully")
            .build());
    }

    @GetMapping("/users")
    public ResponseEntity<Response<List<AdminUserDto>>> getAdminUsers(
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        adminAuthorizationService.assertAdmin(userDetails);

        return ResponseEntity.ok(Response.<List<AdminUserDto>>builder()
            .data(adminUserService.getGrantableUsers())
            .message("Admin users fetched successfully")
            .build());
    }

    @GetMapping("/nft/avatar-items")
    public ResponseEntity<Response<List<AdminNftItemDto>>> getMintedNftAvatarItems(
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        adminAuthorizationService.assertAdmin(userDetails);

        return ResponseEntity.ok(Response.<List<AdminNftItemDto>>builder()
            .data(adminNftService.getMintedAvatarItems())
            .message("Admin NFT avatar items fetched successfully")
            .build());
    }

    @GetMapping("/nft/avatar-items/try-on")
    public ResponseEntity<Response<List<NftAvatarItemDto>>> getTryOnNftAvatarItems(
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        adminAuthorizationService.assertAdmin(userDetails);

        return ResponseEntity.ok(Response.<List<NftAvatarItemDto>>builder()
            .data(adminNftService.getTryOnAvatarItems())
            .message("Admin NFT avatar try-on items fetched successfully")
            .build());
    }

    @PostMapping("/nft/avatar-items/grants")
    public ResponseEntity<Response<AdminNftGrantResponseDto>> grantNftAvatarItem(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @Valid @RequestBody AdminNftGrantRequestDto request
    ) {
        adminAuthorizationService.assertAdmin(userDetails);

        return ResponseEntity.ok(Response.<AdminNftGrantResponseDto>builder()
            .data(adminNftService.grantAvatarItem(request))
            .message("Admin NFT avatar item granted successfully")
            .build());
    }

    @GetMapping("/challenges")
    public ResponseEntity<Response<List<AdminChallengeDto>>> getAdminChallenges(
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        adminAuthorizationService.assertAdmin(userDetails);

        return ResponseEntity.ok(Response.<List<AdminChallengeDto>>builder()
            .data(adminChallengeService.getChallenges())
            .message("Admin challenges fetched successfully")
            .build());
    }

    @GetMapping("/challenges/{challengeId}")
    public ResponseEntity<Response<AdminChallengeDto>> getAdminChallenge(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable Long challengeId
    ) {
        adminAuthorizationService.assertAdmin(userDetails);

        return ResponseEntity.ok(Response.<AdminChallengeDto>builder()
            .data(adminChallengeService.getChallenge(challengeId))
            .message("Admin challenge fetched successfully")
            .build());
    }

    @PostMapping("/challenges")
    public ResponseEntity<Response<AdminChallengeDto>> createAdminChallenge(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @Valid @RequestBody AdminChallengeRequestDto request
    ) {
        adminAuthorizationService.assertAdmin(userDetails);

        return ResponseEntity.ok(Response.<AdminChallengeDto>builder()
            .data(adminChallengeService.createChallenge(request))
            .message("Admin challenge created successfully")
            .build());
    }

    @PutMapping("/challenges/{challengeId}")
    public ResponseEntity<Response<AdminChallengeDto>> updateAdminChallenge(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable Long challengeId,
        @Valid @RequestBody AdminChallengeRequestDto request
    ) {
        adminAuthorizationService.assertAdmin(userDetails);

        return ResponseEntity.ok(Response.<AdminChallengeDto>builder()
            .data(adminChallengeService.updateChallenge(challengeId, request))
            .message("Admin challenge updated successfully")
            .build());
    }

    @DeleteMapping("/challenges/{challengeId}")
    public ResponseEntity<Response<Void>> deleteAdminChallenge(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable Long challengeId
    ) {
        adminAuthorizationService.assertAdmin(userDetails);
        adminChallengeService.deleteChallenge(challengeId);

        return ResponseEntity.ok(Response.<Void>builder()
            .message("Admin challenge deleted successfully")
            .build());
    }
}
