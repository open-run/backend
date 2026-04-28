package io.openur.domain.NFT.controller;

import io.openur.domain.NFT.service.NFTService;
import io.openur.domain.NFT.dto.NFTMetadataDto;
import io.openur.domain.NFT.dto.NftAvatarItemDto;
import io.openur.domain.NFT.dto.NftWearingAvatarDto;
import io.openur.domain.NFT.dto.NftWearingAvatarRequestDto;
import io.openur.domain.NFT.service.NftAvatarItemService;
import io.openur.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import io.openur.global.dto.PagedResponse;
import io.openur.global.dto.Response;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/nft")
@RequiredArgsConstructor
public class NFTController {

    private final NFTService nftService;
    private final NftAvatarItemService nftAvatarItemService;

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

    @GetMapping("/avatar-items/me")
    public ResponseEntity<Response<List<NftAvatarItemDto>>> getMyNftAvatarItems(
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        String userAddress = userDetails.getUser().getBlockchainAddress();
        List<NftAvatarItemDto> avatarItems = nftAvatarItemService.getOwnedAvatarItems(userAddress);

        return ResponseEntity.ok().body(Response.<List<NftAvatarItemDto>>builder()
            .data(avatarItems)
            .message("Owned NFT avatar items fetched successfully")
            .build());
    }

    @GetMapping("/avatar-items/me/wearing")
    public ResponseEntity<Response<NftWearingAvatarDto>> getMyWearingNftAvatar(
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        NftWearingAvatarDto wearingAvatar = nftAvatarItemService.getWearingAvatar(userDetails.getUser().getUserId());

        return ResponseEntity.ok().body(Response.<NftWearingAvatarDto>builder()
            .data(wearingAvatar)
            .message("Wearing NFT avatar fetched successfully")
            .build());
    }

    @PutMapping("/avatar-items/me/wearing")
    public ResponseEntity<Response<NftWearingAvatarDto>> saveMyWearingNftAvatar(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestBody NftWearingAvatarRequestDto request
    ) {
        NftWearingAvatarDto wearingAvatar = nftAvatarItemService.saveWearingAvatar(
            userDetails.getUser().getUserId(),
            userDetails.getUser().getBlockchainAddress(),
            request
        );

        return ResponseEntity.ok().body(Response.<NftWearingAvatarDto>builder()
            .data(wearingAvatar)
            .message("Wearing NFT avatar saved successfully")
            .build());
    }
}
