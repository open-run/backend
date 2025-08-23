package io.openur.domain.challenge.controller;

import io.openur.domain.challenge.dto.ChallengeInfoDto;
import io.openur.domain.challenge.model.CompletedType;
import io.openur.domain.challenge.service.ChallengeService;
import io.openur.global.common.PagedResponse;
import io.openur.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/challenges")
@RequiredArgsConstructor
public class ChallengeController {
    private final ChallengeService challengeService;

    @GetMapping()
    public ResponseEntity<PagedResponse<ChallengeInfoDto>> getMyChallengeList(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestParam(required = false, defaultValue = "") CompletedType type,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "10") int limit
    ) {
        Pageable pageable = PageRequest.of(page, limit);

        Page<ChallengeInfoDto> challenges = challengeService.getMyChallengeList(
            userDetails, type, pageable
        );

        return ResponseEntity.ok(
            PagedResponse.build(challenges, "sucess")
        );
    }
}
