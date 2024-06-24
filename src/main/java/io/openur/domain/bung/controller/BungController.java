package io.openur.domain.bung.controller;

import io.openur.domain.bung.dto.GetBungDetailDto;
import io.openur.domain.bung.dto.PostBungEntityDto;
import io.openur.domain.bung.service.BungService;
import io.openur.global.common.Response;
import io.openur.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
