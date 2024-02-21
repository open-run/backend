package io.openur.domain.user.controller;

import io.openur.domain.user.dto.testDto;
import io.openur.domain.user.service.UserService;
import io.openur.global.common.Response;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/v1/test")
    @Operation(summary = "테스트 api")
    public ResponseEntity<Response<testDto>> test(){

        return ResponseEntity.ok()
            .body(Response.<testDto>builder()
                .message("test")
                .data(new testDto("test"))
                .build());
    }
}
