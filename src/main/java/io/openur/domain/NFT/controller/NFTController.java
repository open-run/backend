package io.openur.domain.NFT.controller;

import io.openur.domain.NFT.service.NFTService;
import io.openur.domain.NFT.dto.NFTMetadataDto;
import io.openur.global.security.UserDetailsImpl; // 패키지 경로는 실제 프로젝트에 맞게 수정
import io.openur.global.common.Response;      // 공통 Response DTO
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/nft")
public class NFTController {

    private final NFTService nftService;

    public NFTController(NFTService nftService) {
        this.nftService = nftService;
    }

    @PostMapping("/mint")
    public ResponseEntity<Response<NFTMetadataDto>> mintNFT(
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) throws Exception {
        NFTMetadataDto metadata = nftService.mintNFT(userDetails);

        return ResponseEntity.ok().body(Response.<NFTMetadataDto>builder()
            .data(metadata)
            .message("NFT minted successfully")
            .build());
    }

}
