package io.openur.domain.NFT.service;

import io.openur.domain.NFT.dto.NftAvatarItemDto;
import io.openur.domain.NFT.dto.NftWearingAvatarDto;
import io.openur.domain.NFT.dto.NftWearingAvatarRequestDto;
import io.openur.domain.NFT.entity.NftAvatarWearingEntity;
import io.openur.domain.NFT.entity.NftItemEntity;
import io.openur.domain.NFT.entity.NftItemEquipImageEntity;
import io.openur.domain.NFT.enums.NftAvatarWearingSlot;
import io.openur.domain.NFT.repository.NftAvatarWearingJpaRepository;
import io.openur.domain.NFT.repository.NftItemEquipImageJpaRepository;
import io.openur.domain.NFT.repository.NftItemJpaRepository;
import io.openur.domain.user.model.User;
import io.openur.domain.user.repository.UserRepository;
import io.openur.domain.user.service.UserProfileImageStorageService;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class NftAvatarItemService {

    private static final BigInteger ZERO = BigInteger.ZERO;

    private final NftItemJpaRepository nftItemJpaRepository;
    private final NftItemEquipImageJpaRepository nftItemEquipImageJpaRepository;
    private final NftAvatarWearingJpaRepository nftAvatarWearingJpaRepository;
    private final NftBalanceReader nftBalanceReader;
    private final NftAvatarItemViewMapper nftAvatarItemViewMapper;
    private final PlatformTransactionManager transactionManager;
    private final UserRepository userRepository;
    private final UserProfileImageStorageService userProfileImageStorageService;

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

        Map<Long, List<NftItemEquipImageEntity>> equipImagesByItemId = runInReadOnlyTransaction(
            () -> getEquipImagesByItemId(ownedItems)
        );

        return ownedItems.stream()
            .map(item -> nftAvatarItemViewMapper.toDto(
                item,
                balances.get(item.getNftTokenId()),
                equipImagesByItemId.getOrDefault(item.getNftItemId(), Collections.emptyList())
            ))
            .toList();
    }

    public NftWearingAvatarDto getWearingAvatar(String userId) {
        return runInReadOnlyTransaction(() -> {
            List<NftAvatarWearingEntity> wearingEntities = nftAvatarWearingJpaRepository.findByUserId(userId);
            Map<NftAvatarWearingSlot, NftItemEntity> wearingItemsBySlot = wearingEntities.stream()
                .filter(wearingEntity -> Boolean.TRUE.equals(wearingEntity.getNftItemEntity().getEnabled()))
                .filter(wearingEntity -> nftAvatarItemViewMapper.matchesSlot(
                    wearingEntity.getWearingSlot(),
                    wearingEntity.getNftItemEntity()
                ))
                .collect(Collectors.toMap(
                    NftAvatarWearingEntity::getWearingSlot,
                    NftAvatarWearingEntity::getNftItemEntity,
                    (left, right) -> left,
                    () -> new EnumMap<>(NftAvatarWearingSlot.class)
                ));

            return buildWearingAvatarDto(wearingItemsBySlot);
        });
    }

    @Transactional
    public NftWearingAvatarDto saveWearingAvatarWithProfileImage(
        String userId,
        String ownerAddress,
        NftWearingAvatarRequestDto request,
        MultipartFile image
    ) {
        Map<NftAvatarWearingSlot, NftItemEntity> wearingItemsBySlot = resolveWearingItems(ownerAddress, request);
        String storageKey = userProfileImageStorageService.store(userId, image);

        replaceWearingItems(userId, wearingItemsBySlot);

        User user = userRepository.findById(userId);
        user.updateProfileImageStorageKey(storageKey);
        userRepository.update(user);

        return buildWearingAvatarDto(wearingItemsBySlot);
    }

    private Map<NftAvatarWearingSlot, NftItemEntity> resolveWearingItems(
        String ownerAddress,
        NftWearingAvatarRequestDto request
    ) {
        if (request == null) {
            throw new IllegalArgumentException("request body is required");
        }
        if (!StringUtils.hasText(ownerAddress)) {
            throw new IllegalArgumentException("ownerAddress is required");
        }
        if (request.getFullSet() != null) {
            throw new IllegalArgumentException("fullSet is not supported yet");
        }

        Map<NftAvatarWearingSlot, Long> requestedSlotItemIds = request.toSlotItemIds()
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue() != null)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (left, right) -> left,
                () -> new EnumMap<>(NftAvatarWearingSlot.class)
            ));
        Map<Long, NftItemEntity> itemsById = getItemsById(requestedSlotItemIds.values());
        Map<NftAvatarWearingSlot, NftItemEntity> wearingItemsBySlot = new EnumMap<>(NftAvatarWearingSlot.class);

        requestedSlotItemIds.forEach((slot, nftItemId) -> {
            NftItemEntity nftItem = itemsById.get(nftItemId);
            if (nftItem == null || !Boolean.TRUE.equals(nftItem.getEnabled())) {
                throw new IllegalArgumentException("invalid nftItemId: " + nftItemId);
            }
            if (!nftAvatarItemViewMapper.matchesSlot(slot, nftItem)) {
                throw new IllegalArgumentException("nftItemId " + nftItemId + " does not match wearing slot " + slot);
            }

            wearingItemsBySlot.put(slot, nftItem);
        });
        assertOwnedAvatarItems(ownerAddress, wearingItemsBySlot.values());

        return wearingItemsBySlot;
    }

    private void replaceWearingItems(String userId, Map<NftAvatarWearingSlot, NftItemEntity> wearingItemsBySlot) {
        nftAvatarWearingJpaRepository.deleteByUserId(userId);
        nftAvatarWearingJpaRepository.saveAll(wearingItemsBySlot.entrySet()
            .stream()
            .map(entry -> new NftAvatarWearingEntity(userId, entry.getKey(), entry.getValue()))
            .toList());
    }

    private List<NftItemEntity> getNftItemCandidates() {
        return nftItemJpaRepository.findByEnabledTrueAndNftTokenIdIsNotNullOrderByNftItemIdAsc()
            .stream()
            .filter(item -> StringUtils.hasText(item.getNftTokenId()))
            .toList();
    }

    private Map<Long, NftItemEntity> getItemsById(Collection<Long> nftItemIds) {
        if (nftItemIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return nftItemJpaRepository.findByNftItemIdIn(nftItemIds).stream()
            .collect(Collectors.toMap(NftItemEntity::getNftItemId, item -> item));
    }

    private void assertOwnedAvatarItems(String ownerAddress, Collection<NftItemEntity> nftItems) {
        if (nftItems.isEmpty()) {
            return;
        }

        boolean hasUnmintedItem = nftItems.stream()
            .anyMatch(nftItem -> !StringUtils.hasText(nftItem.getNftTokenId()));
        if (hasUnmintedItem) {
            throw new IllegalArgumentException("wearing nftItem has no minted token");
        }

        List<String> tokenIds = nftItems.stream()
            .map(NftItemEntity::getNftTokenId)
            .distinct()
            .toList();

        Map<String, BigInteger> balances = nftBalanceReader.getBalances(ownerAddress, tokenIds);
        nftItems.forEach(nftItem -> {
            BigInteger balance = balances.getOrDefault(nftItem.getNftTokenId(), ZERO);
            if (balance.compareTo(ZERO) <= 0) {
                throw new IllegalArgumentException("nftItemId is not owned by user: " + nftItem.getNftItemId());
            }
        });
    }

    private Map<Long, List<NftItemEquipImageEntity>> getEquipImagesByItemId(List<NftItemEntity> nftItems) {
        List<Long> nftItemIds = nftItems.stream()
            .map(NftItemEntity::getNftItemId)
            .toList();
        List<NftItemEquipImageEntity> equipImages = nftItemEquipImageJpaRepository
            .findByNftItemEntity_NftItemIdInOrderByNftItemEntity_NftItemIdAscSortOrderAscNftItemEquipImageIdAsc(nftItemIds);

        return equipImages.stream()
            .collect(Collectors.groupingBy(
                equipImage -> equipImage.getNftItemEntity().getNftItemId(),
                LinkedHashMap::new,
                Collectors.toList()
            ));
    }

    private NftWearingAvatarDto buildWearingAvatarDto(Map<NftAvatarWearingSlot, NftItemEntity> wearingItemsBySlot) {
        if (wearingItemsBySlot.isEmpty()) {
            return NftWearingAvatarDto.empty();
        }

        Map<Long, List<NftItemEquipImageEntity>> equipImagesByItemId = getEquipImagesByItemId(
            wearingItemsBySlot.values().stream().toList()
        );

        return NftWearingAvatarDto.builder()
            .fullSet(null)
            .upperClothing(toWearingItemDto(NftAvatarWearingSlot.upper_clothing, wearingItemsBySlot, equipImagesByItemId))
            .lowerClothing(toWearingItemDto(NftAvatarWearingSlot.lower_clothing, wearingItemsBySlot, equipImagesByItemId))
            .footwear(toWearingItemDto(NftAvatarWearingSlot.footwear, wearingItemsBySlot, equipImagesByItemId))
            .face(toWearingItemDto(NftAvatarWearingSlot.face, wearingItemsBySlot, equipImagesByItemId))
            .skin(toWearingItemDto(NftAvatarWearingSlot.skin, wearingItemsBySlot, equipImagesByItemId))
            .hair(toWearingItemDto(NftAvatarWearingSlot.hair, wearingItemsBySlot, equipImagesByItemId))
            .accessories(NftWearingAvatarDto.Accessories.builder()
                .headAccessories(toWearingItemDto(NftAvatarWearingSlot.head_accessories, wearingItemsBySlot, equipImagesByItemId))
                .eyeAccessories(toWearingItemDto(NftAvatarWearingSlot.eye_accessories, wearingItemsBySlot, equipImagesByItemId))
                .earAccessories(toWearingItemDto(NftAvatarWearingSlot.ear_accessories, wearingItemsBySlot, equipImagesByItemId))
                .bodyAccessories(toWearingItemDto(NftAvatarWearingSlot.body_accessories, wearingItemsBySlot, equipImagesByItemId))
                .build())
            .build();
    }

    private NftAvatarItemDto toWearingItemDto(
        NftAvatarWearingSlot slot,
        Map<NftAvatarWearingSlot, NftItemEntity> wearingItemsBySlot,
        Map<Long, List<NftItemEquipImageEntity>> equipImagesByItemId
    ) {
        NftItemEntity nftItem = wearingItemsBySlot.get(slot);
        if (nftItem == null) {
            return null;
        }

        return nftAvatarItemViewMapper.toDto(
            nftItem,
            null,
            equipImagesByItemId.getOrDefault(nftItem.getNftItemId(), Collections.emptyList())
        );
    }

    private <T> T runInReadOnlyTransaction(Supplier<T> callback) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setReadOnly(true);
        return transactionTemplate.execute(status -> callback.get());
    }
}
