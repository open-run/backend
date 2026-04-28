package io.openur.domain.admin.service;

import io.openur.domain.NFT.entity.NftItemEntity;
import io.openur.domain.NFT.repository.NftItemJpaRepository;
import io.openur.domain.NFT.service.NftAssetUrlResolver;
import io.openur.domain.NFT.service.NftAvatarItemViewMapper;
import io.openur.domain.NFT.service.NftMintClient;
import io.openur.domain.admin.dto.AdminNftGrantRequestDto;
import io.openur.domain.admin.dto.AdminNftGrantResponseDto;
import io.openur.domain.admin.dto.AdminNftItemDto;
import java.math.BigInteger;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminNftService {

    private static final BigInteger GRANT_AMOUNT = BigInteger.ONE;

    private final NftItemJpaRepository nftItemJpaRepository;
    private final NftAssetUrlResolver nftAssetUrlResolver;
    private final NftAvatarItemViewMapper nftAvatarItemViewMapper;
    private final NftMintClient nftMintClient;

    @Transactional(readOnly = true)
    public List<AdminNftItemDto> getMintedAvatarItems() {
        return nftItemJpaRepository.findByEnabledTrueAndNftTokenIdIsNotNullOrderByNftItemIdAsc()
            .stream()
            .map(nftItem -> AdminNftItemDto.from(nftItem, nftAssetUrlResolver, nftAvatarItemViewMapper))
            .toList();
    }

    public AdminNftGrantResponseDto grantAvatarItem(AdminNftGrantRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("request body is required");
        }

        NftItemEntity nftItem = getGrantableNftItem(request.getNftItemId());
        BigInteger tokenId = parseTokenId(nftItem.getNftTokenId());
        String transactionHash = nftMintClient.mintToken(request.getRecipientAddress(), tokenId, GRANT_AMOUNT);

        return AdminNftGrantResponseDto.from(request.getRecipientAddress(), nftItem, transactionHash);
    }

    private NftItemEntity getGrantableNftItem(Long nftItemId) {
        if (nftItemId == null) {
            throw new IllegalArgumentException("nftItemId is required");
        }

        NftItemEntity nftItem = nftItemJpaRepository.findById(nftItemId)
            .orElseThrow(() -> new IllegalArgumentException("invalid nftItemId: " + nftItemId));
        if (!Boolean.TRUE.equals(nftItem.getEnabled())) {
            throw new IllegalArgumentException("disabled nftItemId: " + nftItemId);
        }
        if (!StringUtils.hasText(nftItem.getNftTokenId())) {
            throw new IllegalArgumentException("nftItemId has no minted token: " + nftItemId);
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
