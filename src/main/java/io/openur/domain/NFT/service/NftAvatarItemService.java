package io.openur.domain.NFT.service;

import io.openur.domain.NFT.dto.NftAvatarItemDto;
import io.openur.domain.NFT.dto.NftAvatarItemEquipImageDto;
import io.openur.domain.NFT.entity.NftItemEntity;
import io.openur.domain.NFT.entity.NftItemEquipImageEntity;
import io.openur.domain.NFT.repository.NftItemEquipImageJpaRepository;
import io.openur.domain.NFT.repository.NftItemJpaRepository;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class NftAvatarItemService {

    private static final BigInteger ZERO = BigInteger.ZERO;

    private final NftItemJpaRepository nftItemJpaRepository;
    private final NftItemEquipImageJpaRepository nftItemEquipImageJpaRepository;
    private final NftBalanceReader nftBalanceReader;
    private final NftAssetUrlResolver nftAssetUrlResolver;
    private final PlatformTransactionManager transactionManager;

    public List<NftAvatarItemDto> getOwnedAvatarItems(String ownerAddress) {
        List<NftItemEntity> candidates = runInReadOnlyTransaction(this::getNftItemCandidates);
        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> tokenIds = candidates.stream()
            .map(NftItemEntity::getNftTokenId)
            .distinct()
            .toList();
        Map<String, BigInteger> balances = nftBalanceReader.getBalances(ownerAddress, tokenIds);

        List<NftItemEntity> ownedItems = candidates.stream()
            .filter(item -> balances.getOrDefault(item.getNftTokenId(), ZERO).compareTo(ZERO) > 0)
            .toList();
        if (ownedItems.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, List<NftAvatarItemEquipImageDto>> equipImagesByItemId = runInReadOnlyTransaction(
            () -> getEquipImagesByItemId(ownedItems)
        );

        return ownedItems.stream()
            .map(item -> NftAvatarItemDto.from(
                item,
                balances.get(item.getNftTokenId()),
                equipImagesByItemId.getOrDefault(item.getNftItemId(), Collections.emptyList()),
                nftAssetUrlResolver
            ))
            .toList();
    }

    private List<NftItemEntity> getNftItemCandidates() {
        return nftItemJpaRepository.findByEnabledTrueAndNftTokenIdIsNotNullOrderByNftItemIdAsc()
            .stream()
            .filter(item -> StringUtils.hasText(item.getNftTokenId()))
            .toList();
    }

    private Map<Long, List<NftAvatarItemEquipImageDto>> getEquipImagesByItemId(List<NftItemEntity> ownedItems) {
        List<Long> ownedItemIds = ownedItems.stream()
            .map(NftItemEntity::getNftItemId)
            .toList();
        List<NftItemEquipImageEntity> equipImages = nftItemEquipImageJpaRepository
            .findByNftItemEntity_NftItemIdInOrderByNftItemEntity_NftItemIdAscSortOrderAscNftItemEquipImageIdAsc(ownedItemIds);

        return equipImages.stream()
            .collect(Collectors.groupingBy(
                equipImage -> equipImage.getNftItemEntity().getNftItemId(),
                Collectors.mapping(
                    equipImage -> NftAvatarItemEquipImageDto.from(equipImage, nftAssetUrlResolver),
                    Collectors.toList()
                )
            ));
    }

    private <T> T runInReadOnlyTransaction(Supplier<T> callback) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setReadOnly(true);
        return transactionTemplate.execute(status -> callback.get());
    }
}
