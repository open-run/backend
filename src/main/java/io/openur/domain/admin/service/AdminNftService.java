package io.openur.domain.admin.service;

import io.openur.domain.NFT.dto.NftAvatarItemDto;
import io.openur.domain.NFT.entity.NftTokenEntity;
import io.openur.domain.NFT.enums.NftImageRole;
import io.openur.domain.NFT.repository.NftJpaRepository;
import io.openur.domain.NFT.repository.NftTokenJpaRepository;
import io.openur.domain.NFT.service.NftAssetUrlResolver;
import io.openur.domain.NFT.service.NftAvatarItemViewMapper;
import io.openur.domain.NFT.service.NftMintClient;
import io.openur.domain.admin.dto.AdminNftGrantRequestDto;
import io.openur.domain.admin.dto.AdminNftGrantResponseDto;
import io.openur.domain.admin.dto.AdminNftItemDto;
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
    private final NftMintClient nftMintClient;

    @Transactional(readOnly = true)
    public List<AdminNftItemDto> getMintedAvatarItems() {
        return nftTokenJpaRepository.findByImageRoleOrderByNftNftIdAsc(NftImageRole.thumbnail)
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
