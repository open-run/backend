package io.openur.domain.NFT.controller;

import io.openur.domain.NFT.service.NFTService;
import io.openur.domain.NFT.dto.NFTMetadataDto;
import io.openur.global.security.UserDetailsImpl;
import io.openur.global.common.PagedResponse;
import io.openur.global.common.Response;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestParam(required = true) Long challengeId
    ) {
        String userAddress = userDetails.getUser().getBlockchainAddress();
        NFTMetadataDto metadata = nftService.mintNFT(userAddress, challengeId);
        
        return ResponseEntity.ok().body(Response.<NFTMetadataDto>builder()
            .data(metadata)
            .message("NFT minted successfully")
            .build());
    }

    @GetMapping("/user/{userAddress}")
    public ResponseEntity<PagedResponse<NFTMetadataDto>> getNFTsByUserAddress(
        @PathVariable String userAddress,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "10") int limit
    ) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<NFTMetadataDto> nfts = nftService.getNFTsByUserAddress(userAddress, pageable);
        return ResponseEntity.ok().body(PagedResponse.build(nfts, "All NFTs fetched successfully"));
    }
}
