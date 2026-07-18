package io.openur.domain.admin.service;

import io.openur.domain.NFT.dto.NftAvatarItemDto;
import io.openur.domain.NFT.entity.NftTokenEntity;
import io.openur.domain.NFT.enums.NftImageRole;
import io.openur.domain.NFT.repository.NftJpaRepository;
import io.openur.domain.NFT.repository.NftTokenJpaRepository;
import io.openur.domain.NFT.service.NftAssetUrlResolver;
import io.openur.domain.NFT.service.NftAvatarItemService;
import io.openur.domain.NFT.service.NftAvatarItemViewMapper;
import io.openur.domain.NFT.service.NftMintClient;
import io.openur.domain.admin.dto.AdminNftGrantRequestDto;
import io.openur.domain.admin.dto.AdminNftGrantResponseDto;
import io.openur.domain.admin.dto.AdminNftItemDto;
import io.openur.global.common.validation.EthereumAddressValidator;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminNftService {

    private static final BigInteger GRANT_AMOUNT = BigInteger.ONE;

    private final NftJpaRepository nftJpaRepository;
    private final NftTokenJpaRepository nftTokenJpaRepository;
    private final NftAssetUrlResolver nftAssetUrlResolver;
    private final NftAvatarItemViewMapper nftAvatarItemViewMapper;
    private final NftAvatarItemService nftAvatarItemService;
    private final NftMintClient nftMintClient;
    private final EthereumAddressValidator ethereumAddressValidator;

    @Transactional(readOnly = true)
    public List<AdminNftItemDto> getMintedAvatarItems() {
        // 발급(grant)·보유 조회 모두 avatar role 토큰 기준이므로 목록도 avatar 토큰의 tokenId를 내려준다.
        // thumbnail role 토큰의 tokenId를 내려주면 grant 검증(findByTokenIdAndImageRole(avatar))에서 실패한다.
        return nftTokenJpaRepository.findByImageRoleOrderByNftNftIdAsc(NftImageRole.avatar)
            .stream()
            .map(token -> AdminNftItemDto.from(
                token.getNft(), token.getTokenId(), nftAssetUrlResolver, nftAvatarItemViewMapper))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<NftAvatarItemDto> getTryOnAvatarItems() {
        Map<Integer, String> avatarTokenIdByNftId = nftTokenJpaRepository
            .findByImageRoleOrderByNftNftIdAsc(NftImageRole.avatar)
            .stream()
            .collect(Collectors.toMap(
                token -> token.getNft().getNftId(),
                NftTokenEntity::getTokenId
            ));

        return nftJpaRepository.findAllByOrderByNftIdAsc().stream()
            .map(nft -> nftAvatarItemViewMapper.toDto(
                nft, avatarTokenIdByNftId.get(nft.getNftId()), null))
            .map(this::normalizeEmptyImageUrl)
            .toList();
    }

    public List<NftAvatarItemDto> getOwnedAvatarItems(String ownerAddress) {
        if (!ethereumAddressValidator.isValid(ownerAddress)) {
            throw new IllegalArgumentException("invalid ethereum address");
        }

        try {
            return nftAvatarItemService.getOwnedAvatarItems(ownerAddress);
        } catch (RuntimeException e) {
            // RPC 실패 등 내부 예외 메시지를 어드민 화면에 그대로 노출하지 않는다
            throw new IllegalStateException("블록체인 잔액 조회에 실패했습니다. 잠시 후 다시 시도해 주세요.", e);
        }
    }

    public AdminNftGrantResponseDto grantAvatarItem(AdminNftGrantRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("request body is required");
        }

        NftTokenEntity token = getGrantableAvatarToken(request.getTokenId());
        BigInteger tokenId = parseTokenId(token.getTokenId());
        String transactionHash = nftMintClient.mintToken(request.getRecipientAddress(), tokenId, GRANT_AMOUNT);

        return AdminNftGrantResponseDto.from(request.getRecipientAddress(), token.getTokenId(), transactionHash);
    }

    private NftTokenEntity getGrantableAvatarToken(String tokenId) {
        if (!StringUtils.hasText(tokenId)) {
            throw new IllegalArgumentException("tokenId is required");
        }

        return nftTokenJpaRepository.findByTokenIdAndImageRole(tokenId, NftImageRole.avatar)
            .orElseThrow(() -> new IllegalArgumentException("nftItem has no minted token: " + tokenId));
    }

    private NftAvatarItemDto normalizeEmptyImageUrl(NftAvatarItemDto nftItem) {
        if (nftItem.getImageUrl() instanceof List<?> imageUrls && imageUrls.isEmpty()) {
            return NftAvatarItemDto.builder()
                .id(nftItem.getId())
                .tokenId(nftItem.getTokenId())
                .balance(nftItem.getBalance())
                .name(nftItem.getName())
                .rarity(nftItem.getRarity())
                .mainCategory(nftItem.getMainCategory())
                .subCategory(nftItem.getSubCategory())
                .imageUrl(null)
                .thumbnailUrl(nftItem.getThumbnailUrl())
                .build();
        }

        return nftItem;
    }

    private BigInteger parseTokenId(String tokenId) {
        try {
            return new BigInteger(tokenId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalid tokenId: " + tokenId);
        }
    }
}
