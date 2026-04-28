package io.openur.domain.admin.service;

import io.openur.domain.admin.dto.AdminUserDto;
import io.openur.domain.user.entity.UserEntity;
import io.openur.domain.user.repository.UserJpaRepository;
import io.openur.global.common.validation.EthereumAddressValidator;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserJpaRepository userJpaRepository;
    private final EthereumAddressValidator ethereumAddressValidator;

    @Transactional(readOnly = true)
    public List<AdminUserDto> getGrantableUsers() {
        return userJpaRepository.findAll()
            .stream()
            .filter(user -> !Boolean.TRUE.equals(user.getBlacklisted()))
            .filter(user -> ethereumAddressValidator.isValid(user.getBlockchainAddress()))
            .sorted(Comparator
                .comparing(UserEntity::getNickname, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(UserEntity::getBlockchainAddress))
            .map(AdminUserDto::from)
            .toList();
    }
}
