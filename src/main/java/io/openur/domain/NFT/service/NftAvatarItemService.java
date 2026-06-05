package io.openur.domain.NFT.service;

import io.openur.domain.NFT.dto.NftAvatarItemDto;
import io.openur.domain.NFT.dto.NftWearingAvatarDto;
import io.openur.domain.NFT.dto.NftWearingAvatarRequestDto;
import io.openur.domain.NFT.entity.NftAvatarWearingEntity;
import io.openur.domain.NFT.entity.NftTokenEntity;
import io.openur.domain.NFT.enums.NftAvatarWearingSlot;
import io.openur.domain.NFT.enums.NftImageRole;
import io.openur.domain.NFT.repository.NftAvatarWearingJpaRepository;
import io.openur.domain.NFT.repository.NftTokenJpaRepository;
import io.openur.domain.user.model.User;
import io.openur.domain.user.repository.UserRepository;
import io.openur.domain.user.service.UserProfileImageStorageService;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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

    private final NftTokenJpaRepository nftTokenJpaRepository;
    private final NftAvatarWearingJpaRepository nftAvatarWearingJpaRepository;
    private final NftBalanceReader nftBalanceReader;
    private final NftAvatarItemViewMapper nftAvatarItemViewMapper;
    private final PlatformTransactionManager transactionManager;
    private final UserRepository userRepository;
    private final UserProfileImageStorageService userProfileImageStorageService;

    public List<NftAvatarItemDto> getOwnedAvatarItems(String ownerAddress) {
        List<NftTokenEntity> candidates = runInReadOnlyTransaction(
            () -> nftTokenJpaRepository.findByImageRoleOrderByNftNftIdAsc(NftImageRole.avatar));
        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> tokenIds = candidates.stream()
            .map(NftTokenEntity::getTokenId)
            .distinct()
            .toList();
        Map<String, BigInteger> balances = nftBalanceReader.getBalances(ownerAddress, tokenIds);

        return candidates.stream()
            .filter(token -> balances.getOrDefault(token.getTokenId(), ZERO).compareTo(ZERO) > 0)
            .map(token -> nftAvatarItemViewMapper.toDto(
                token.getNft(),
                token.getTokenId(),
                balances.get(token.getTokenId())
            ))
            .toList();
    }

    public NftWearingAvatarDto getWearingAvatar(String userId) {
        return runInReadOnlyTransaction(() -> {
            List<NftAvatarWearingEntity> wearingEntities = nftAvatarWearingJpaRepository.findByUserId(userId);
            if (wearingEntities.isEmpty()) {
                return NftWearingAvatarDto.empty();
            }

            Map<String, NftTokenEntity> tokensById = getAvatarTokensById(
                wearingEntities.stream().map(NftAvatarWearingEntity::getTokenId).toList());

            Map<NftAvatarWearingSlot, NftTokenEntity> wearingTokensBySlot =
                new EnumMap<>(NftAvatarWearingSlot.class);
            for (NftAvatarWearingEntity wearingEntity : wearingEntities) {
                NftTokenEntity token = tokensById.get(wearingEntity.getTokenId());
                if (token == null) {
                    continue;
                }
                if (!nftAvatarItemViewMapper.matchesSlot(wearingEntity.getWearingSlot(), token.getNft())) {
                    continue;
                }
                wearingTokensBySlot.putIfAbsent(wearingEntity.getWearingSlot(), token);
            }

            return buildWearingAvatarDto(wearingTokensBySlot);
        });
    }

    @Transactional
    public NftWearingAvatarDto saveWearingAvatarWithProfileImage(
        String userId,
        String ownerAddress,
        NftWearingAvatarRequestDto request,
        MultipartFile image
    ) {
        Map<NftAvatarWearingSlot, NftTokenEntity> wearingTokensBySlot = resolveWearingItems(ownerAddress, request);
        String storageKey = userProfileImageStorageService.store(userId, image);

        replaceWearingItems(userId, wearingTokensBySlot);

        User user = userRepository.findById(userId);
        user.updateProfileImageStorageKey(storageKey);
        userRepository.update(user);

        return buildWearingAvatarDto(wearingTokensBySlot);
    }

    private Map<NftAvatarWearingSlot, NftTokenEntity> resolveWearingItems(
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

        Map<NftAvatarWearingSlot, String> requestedSlotTokenIds = request.toSlotTokenIds()
            .entrySet()
            .stream()
            .filter(entry -> StringUtils.hasText(entry.getValue()))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (left, right) -> left,
                () -> new EnumMap<>(NftAvatarWearingSlot.class)
            ));
        Map<String, NftTokenEntity> tokensById = getAvatarTokensById(requestedSlotTokenIds.values());
        Map<NftAvatarWearingSlot, NftTokenEntity> wearingTokensBySlot =
            new EnumMap<>(NftAvatarWearingSlot.class);

        requestedSlotTokenIds.forEach((slot, tokenId) -> {
            NftTokenEntity token = tokensById.get(tokenId);
            if (token == null) {
                throw new IllegalArgumentException("invalid tokenId: " + tokenId);
            }
            if (!nftAvatarItemViewMapper.matchesSlot(slot, token.getNft())) {
                throw new IllegalArgumentException(
                    "tokenId " + tokenId + " does not match wearing slot " + slot);
            }

            wearingTokensBySlot.put(slot, token);
        });
        assertOwnedAvatarItems(ownerAddress, wearingTokensBySlot.values());

        return wearingTokensBySlot;
    }

    private void replaceWearingItems(
        String userId,
        Map<NftAvatarWearingSlot, NftTokenEntity> wearingTokensBySlot
    ) {
        nftAvatarWearingJpaRepository.deleteByUserId(userId);
        nftAvatarWearingJpaRepository.saveAll(wearingTokensBySlot.entrySet()
            .stream()
            .map(entry -> new NftAvatarWearingEntity(userId, entry.getKey(), entry.getValue().getTokenId()))
            .toList());
    }

    private Map<String, NftTokenEntity> getAvatarTokensById(Collection<String> tokenIds) {
        if (tokenIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return nftTokenJpaRepository.findByTokenIdInAndImageRole(tokenIds, NftImageRole.avatar)
            .stream()
            .collect(Collectors.toMap(NftTokenEntity::getTokenId, Function.identity()));
    }

    private void assertOwnedAvatarItems(String ownerAddress, Collection<NftTokenEntity> tokens) {
        if (tokens.isEmpty()) {
            return;
        }

        List<String> tokenIds = tokens.stream()
            .map(NftTokenEntity::getTokenId)
            .distinct()
            .toList();

        Map<String, BigInteger> balances = nftBalanceReader.getBalances(ownerAddress, tokenIds);
        tokens.forEach(token -> {
            BigInteger balance = balances.getOrDefault(token.getTokenId(), ZERO);
            if (balance.compareTo(ZERO) <= 0) {
                throw new IllegalArgumentException("tokenId is not owned by user: " + token.getTokenId());
            }
        });
    }

    private NftWearingAvatarDto buildWearingAvatarDto(
        Map<NftAvatarWearingSlot, NftTokenEntity> wearingTokensBySlot
    ) {
        if (wearingTokensBySlot.isEmpty()) {
            return NftWearingAvatarDto.empty();
        }

        return NftWearingAvatarDto.builder()
            .fullSet(null)
            .upperClothing(toWearingItemDto(NftAvatarWearingSlot.upper_clothing, wearingTokensBySlot))
            .lowerClothing(toWearingItemDto(NftAvatarWearingSlot.lower_clothing, wearingTokensBySlot))
            .footwear(toWearingItemDto(NftAvatarWearingSlot.footwear, wearingTokensBySlot))
            .face(toWearingItemDto(NftAvatarWearingSlot.face, wearingTokensBySlot))
            .skin(toWearingItemDto(NftAvatarWearingSlot.skin, wearingTokensBySlot))
            .hair(toWearingItemDto(NftAvatarWearingSlot.hair, wearingTokensBySlot))
            .accessories(NftWearingAvatarDto.Accessories.builder()
                .headAccessories(toWearingItemDto(NftAvatarWearingSlot.head_accessories, wearingTokensBySlot))
                .eyeAccessories(toWearingItemDto(NftAvatarWearingSlot.eye_accessories, wearingTokensBySlot))
                .earAccessories(toWearingItemDto(NftAvatarWearingSlot.ear_accessories, wearingTokensBySlot))
                .bodyAccessories(toWearingItemDto(NftAvatarWearingSlot.body_accessories, wearingTokensBySlot))
                .build())
            .build();
    }

    private NftAvatarItemDto toWearingItemDto(
        NftAvatarWearingSlot slot,
        Map<NftAvatarWearingSlot, NftTokenEntity> wearingTokensBySlot
    ) {
        NftTokenEntity token = wearingTokensBySlot.get(slot);
        if (token == null) {
            return null;
        }

        return nftAvatarItemViewMapper.toDto(token.getNft(), token.getTokenId(), null);
    }

    private <T> T runInReadOnlyTransaction(Supplier<T> callback) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setReadOnly(true);
        return transactionTemplate.execute(status -> callback.get());
    }
}
