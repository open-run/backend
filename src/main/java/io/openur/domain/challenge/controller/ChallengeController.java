package io.openur.domain.challenge.controller;

import io.openur.domain.challenge.dto.GeneralChallengeDto;
import io.openur.domain.challenge.enums.CompletedType;
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

    @GetMapping("/general")
    public ResponseEntity<PagedResponse<GeneralChallengeDto>>
    getGeneralChallengeList(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "10") int limit
    ) {
        Pageable pageable = PageRequest.of(page, limit);

        Page<GeneralChallengeDto> challenges = challengeService.getGeneralChallengeList(
            userDetails, pageable
        );

        return ResponseEntity.ok(
            PagedResponse.build(challenges, "success")
        );
    }

    @GetMapping("/completed")
    public ResponseEntity<PagedResponse<GeneralChallengeDto>>
    getCompletedChallengeList(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "10") int limit
    ) {
        Pageable pageable = PageRequest.of(page, limit);

        Page<GeneralChallengeDto> challenges = challengeService.getCompletedChallengeList(
            userDetails, pageable
        );

        return ResponseEntity.ok(
            PagedResponse.build(challenges, "success")
        );
    }

    //TODO : 도전과제 테이블을 변경 우선해 수행
    @GetMapping("/repetitive")
    public ResponseEntity<PagedResponse<GeneralChallengeDto>>
    getRepetitiveChallengeList(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "10") int limit
    ) {
        Pageable pageable = PageRequest.of(page, limit);

        Page<GeneralChallengeDto> challenges = challengeService.getRepetitiveChallengeList(
            userDetails, pageable
        );

        return ResponseEntity.ok(
            PagedResponse.build(challenges, "success")
        );
    }
}
