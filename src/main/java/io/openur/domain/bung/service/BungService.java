package io.openur.domain.bung.service;

import io.openur.domain.bung.dto.GetBungDetailDto;
import io.openur.domain.bung.dto.PostBungEntityDto;
import io.openur.domain.bung.entity.BungEntity;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.bung.repository.BungRepositoryImpl;
import io.openur.domain.user.entity.UserEntity;
import io.openur.domain.user.repository.UserRepositoryImpl;
import io.openur.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BungService {
    private final BungRepositoryImpl bungRepository;
    private final UserRepositoryImpl userRepository;

    public GetBungDetailDto createBungEntity(@AuthenticationPrincipal UserDetailsImpl userDetails,
        PostBungEntityDto dto) {
        UserEntity userEntity = userRepository.findByEmail(userDetails.getUser().getEmail());

        Bung bung = new Bung(dto);
        bungRepository.save(bung.toEntity());

        return new GetBungDetailDto(bung);
    }

    public GetBungDetailDto getBungDetail(UserDetailsImpl userDetails,
        String bungId) {

        return new GetBungDetailDto(Bung.from(bungRepository.findByBungId(bungId)));
    }
}
