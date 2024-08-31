package io.openur.domain.xrpl.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.openur.domain.xrpl.dto.NftDataDto;
import io.openur.domain.xrpl.service.XrplService;
import io.openur.global.common.Response;
import io.openur.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;

@RestController
@RequestMapping("/v1/xrpls")
@RequiredArgsConstructor
public class XrplController {
    private final XrplService xrplService;

    @PostMapping("/mint")
    @Operation(summary = "NFT minting")
    public ResponseEntity<Response<NftDataDto>> MintNft(
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) throws InterruptedException, JsonRpcClientErrorException, JsonProcessingException {
        NftDataDto nftDataDto = xrplService.mintNft(userDetails);
        return ResponseEntity.ok()
            .body(Response.<NftDataDto>builder()
                .message("success")
                .data(nftDataDto)
                .build());
    }
}
