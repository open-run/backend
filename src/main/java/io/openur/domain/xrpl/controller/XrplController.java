package io.openur.domain.xrpl.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.openur.domain.xrpl.dto.NftDataDto;
import io.openur.domain.xrpl.service.XrplService;
import io.openur.global.common.Response;
import io.openur.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ResponseEntity<Response<NftDataDto>> mintNft(
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) throws InterruptedException, JsonRpcClientErrorException, JsonProcessingException {
        NftDataDto nftDataDto = xrplService.mintNft(userDetails);
        return ResponseEntity.ok()
            .body(Response.<NftDataDto>builder()
                .message("success")
                .data(nftDataDto)
                .build());
    }

    @GetMapping("/nfts")
    @Operation(summary = "Get NFT list owned by user")
    public ResponseEntity<Response<List<NftDataDto>>> getNftList(
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) throws JsonRpcClientErrorException {
        List<NftDataDto> nftDataDto = xrplService.getNftList(userDetails);

        return ResponseEntity.ok()
            .body(Response.<List<NftDataDto>>builder()
                .message("success")
                .data(nftDataDto)
                .build());
    }
}
