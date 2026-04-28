package io.openur.domain.admin.controller;

import io.openur.domain.admin.dto.AdminMeDto;
import io.openur.domain.admin.dto.AdminNftGrantRequestDto;
import io.openur.domain.admin.dto.AdminNftGrantResponseDto;
import io.openur.domain.admin.dto.AdminNftItemDto;
import io.openur.domain.admin.dto.AdminUserDto;
import io.openur.domain.admin.service.AdminAuthorizationService;
import io.openur.domain.admin.service.AdminNftService;
import io.openur.domain.admin.service.AdminUserService;
import io.openur.global.dto.Response;
import io.openur.global.security.UserDetailsImpl;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminAuthorizationService adminAuthorizationService;
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
}
